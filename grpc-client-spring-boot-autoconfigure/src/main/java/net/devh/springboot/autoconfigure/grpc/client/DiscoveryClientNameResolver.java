/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.springboot.autoconfigure.grpc.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.grpc.Attributes;
import io.grpc.Channel;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * The DiscoveryClientNameResolver configures the hosts and the associated ports for {@link Channel}s to use based on a
 * {@link DiscoveryClient}.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Slf4j
public class DiscoveryClientNameResolver extends NameResolver {

    private final String name;
    private final DiscoveryClient client;
    private final Attributes attributes;
    private final SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;
    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private ScheduledExecutorService timerService;
    @GuardedBy("this")
    private ExecutorService executor;
    @GuardedBy("this")
    private ScheduledFuture<?> resolutionTask;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;
    @GuardedBy("this")
    private List<ServiceInstance> serviceInstanceList;

    public DiscoveryClientNameResolver(String name, DiscoveryClient client, Attributes attributes,
            SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource,
            SharedResourceHolder.Resource<ExecutorService> executorResource) {
        this.name = name;
        this.client = client;
        this.attributes = attributes;
        this.timerServiceResource = timerServiceResource;
        this.executorResource = executorResource;
        this.serviceInstanceList = Lists.newArrayList();
    }

    @Override
    public final String getServiceAuthority() {
        return name;
    }

    @Override
    public final synchronized void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "already started");
        timerService = SharedResourceHolder.get(timerServiceResource);
        executor = SharedResourceHolder.get(executorResource);
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }

    @Override
    public final synchronized void refresh() {
        if (listener != null) {
            resolve();
        }
    }

    private final Runnable resolutionRunnable = new Runnable() {
        @Override
        public void run() {
            Listener savedListener;
            synchronized (DiscoveryClientNameResolver.this) {
                // If this task is started by refresh(), there might already be a scheduled task.
                if (resolutionTask != null) {
                    resolutionTask.cancel(false);
                    resolutionTask = null;
                }
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }
            try {
                List<ServiceInstance> newServiceInstanceList;
                try {
                    newServiceInstanceList = client.getInstances(name);
                } catch (Exception e) {
                    savedListener.onError(Status.UNAVAILABLE.withCause(e));
                    return;
                }

                if (CollectionUtils.isNotEmpty(newServiceInstanceList)) {
                    if (isNeedToUpdateServiceInstanceList(newServiceInstanceList)) {
                        serviceInstanceList = newServiceInstanceList;
                    } else {
                        return;
                    }
                    List<EquivalentAddressGroup> equivalentAddressGroups = Lists.newArrayList();
                    for (ServiceInstance serviceInstance : serviceInstanceList) {
                        Map<String, String> metadata = serviceInstance.getMetadata();
                        if (metadata.get("gRPC.port") != null) {
                            Integer port = Integer.valueOf(metadata.get("gRPC.port"));
                            log.info("Found gRPC server {} {}:{}", name, serviceInstance.getHost(), port);
                            EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(
                                    new InetSocketAddress(serviceInstance.getHost(), port), Attributes.EMPTY);
                            equivalentAddressGroups.add(addressGroup);
                        } else {
                            log.error("Can not found gRPC server {}", name);
                        }
                    }
                    savedListener.onAddresses(equivalentAddressGroups, Attributes.EMPTY);
                } else {
                    savedListener.onError(Status.UNAVAILABLE
                            .withCause(new RuntimeException("UNAVAILABLE: NameResolver returned an empty list")));
                }
            } finally {
                synchronized (DiscoveryClientNameResolver.this) {
                    resolving = false;
                }
            }
        }
    };

    private boolean isNeedToUpdateServiceInstanceList(List<ServiceInstance> newServiceInstanceList) {
        if (serviceInstanceList.size() == newServiceInstanceList.size()) {
            for (ServiceInstance serviceInstance : serviceInstanceList) {
                boolean isSame = false;
                for (ServiceInstance newServiceInstance : newServiceInstanceList) {
                    if (newServiceInstance.getHost().equals(serviceInstance.getHost())
                            && newServiceInstance.getPort() == serviceInstance.getPort()) {
                        isSame = true;
                        break;
                    }
                }
                if (!isSame) {
                    log.info("Ready to update {} server info group list", name);
                    return true;
                }
            }
        } else {
            log.info("Ready to update {} server info group list", name);
            return true;
        }
        return false;
    }

    @GuardedBy("this")
    private void resolve() {
        if (resolving || shutdown) {
            return;
        }
        executor.execute(resolutionRunnable);
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (resolutionTask != null) {
            resolutionTask.cancel(false);
        }
        if (timerService != null) {
            timerService = SharedResourceHolder.release(timerServiceResource, timerService);
        }
        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }
    }

}

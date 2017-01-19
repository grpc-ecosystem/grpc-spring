package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.internal.LogExceptionRunnable;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
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

    public DiscoveryClientNameResolver(String name, DiscoveryClient client, Attributes attributes, SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource,
                                       SharedResourceHolder.Resource<ExecutorService> executorResource) {
        this.name = name;
        this.client = client;
        this.attributes = attributes;
        this.timerServiceResource = timerServiceResource;
        this.executorResource = executorResource;
    }

    @Override
    public final String getServiceAuthority() {
        return name;
    }

    @Override
    public final synchronized void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "already started");
        timerService = SharedResourceHolder.get(timerServiceResource);
        this.listener = listener;
        executor = SharedResourceHolder.get(executorResource);
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }

    @Override
    public final synchronized void refresh() {
        Preconditions.checkState(listener != null, "not started");
        resolve();
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
                List<ServiceInstance> serviceInstanceList;
                try {
                    serviceInstanceList = client.getInstances(name);
                } catch (Exception e) {
                    synchronized (DiscoveryClientNameResolver.this) {
                        if (shutdown) {
                            return;
                        }
                        // Because timerService is the single-threaded GrpcUtil.TIMER_SERVICE in production,
                        // we need to delegate the blocking work to the executor
                        resolutionTask = timerService.schedule(new LogExceptionRunnable(resolutionRunnableOnExecutor), 1, TimeUnit.MINUTES);
                    }
                    savedListener.onError(Status.UNAVAILABLE.withCause(e));
                    return;
                }

                List<List<ResolvedServerInfo>> serversList = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(serviceInstanceList)) {
                    for (ServiceInstance serviceInstance : serviceInstanceList) {
                        List<ResolvedServerInfo> servers = new ArrayList<>();
                        Map<String, String> metadata = serviceInstance.getMetadata();
                        if (metadata.get("grpc") != null) {
                            Integer port = Integer.valueOf(metadata.get("grpc"));
                            log.info("Found grpc server {} {}:{}", name, serviceInstance.getHost(), port);
                            servers.add(new ResolvedServerInfo(InetSocketAddress.createUnresolved(serviceInstance.getHost(), port), Attributes.EMPTY));
                        } else {
                            log.error("Can not found grpc server {}", name);
                        }
                        serversList.add(servers);
                    }
                    savedListener.onUpdate(serversList, Attributes.EMPTY);
                }

            } finally {
                synchronized (DiscoveryClientNameResolver.this) {
                    resolving = false;
                }
            }
        }
    };

    private final Runnable resolutionRunnableOnExecutor = new Runnable() {
        @Override
        public void run() {
            synchronized (DiscoveryClientNameResolver.this) {
                if (!shutdown) {
                    executor.execute(resolutionRunnable);
                }
            }
        }
    };

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

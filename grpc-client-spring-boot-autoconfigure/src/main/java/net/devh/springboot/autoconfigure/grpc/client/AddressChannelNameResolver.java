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
import java.util.concurrent.ExecutorService;

import javax.annotation.concurrent.GuardedBy;

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
 * The AddressChannelNameResolver configures the hosts and the associated ports for {@link Channel}s to use based on
 * {@link GrpcChannelProperties}.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Slf4j
public class AddressChannelNameResolver extends NameResolver {

    private final String name;
    private final GrpcChannelProperties properties;
    private final Attributes attributes;

    private final SharedResourceHolder.Resource<ExecutorService> executorResource;
    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private ExecutorService executor;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;

    public AddressChannelNameResolver(String name, GrpcChannelProperties properties, Attributes attributes,
            SharedResourceHolder.Resource<ExecutorService> executorResource) {
        this.name = name;
        this.properties = properties;
        this.attributes = attributes;
        this.executorResource = executorResource;
    }

    @Override
    public String getServiceAuthority() {
        return name;
    }

    @Override
    public final synchronized void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "already started");
        executor = SharedResourceHolder.get(executorResource);
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }

    private <T> void replace(final List<T> list, final int max, final T defaultValue) {
        list.replaceAll(e -> e == null ? defaultValue : e);
        for (int i = list.size(); i < max; i++) {
            list.add(defaultValue);
        }
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
            synchronized (AddressChannelNameResolver.this) {
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }
            try {
                int max = Math.max(properties.getHost().size(), properties.getPort().size());
                replace(properties.getHost(), max, GrpcChannelProperties.DEFAULT_HOST);
                replace(properties.getPort(), max, GrpcChannelProperties.DEFAULT_PORT);

                if (properties.getHost().size() != properties.getPort().size()) {
                    log.error(
                            "config gRPC server {} error, hosts length isn't equals ports length,hosts [{}], ports [{}]",
                            properties.getHost(), properties.getPort());
                    savedListener.onError(Status.UNAVAILABLE.withCause(new RuntimeException("gRPC config error")));
                    return;
                }

                List<EquivalentAddressGroup> equivalentAddressGroups = Lists.newArrayList();
                for (int i = 0; i < properties.getHost().size(); i++) {
                    String host = properties.getHost().get(i);
                    Integer port = properties.getPort().get(i);
                    log.info("Found gRPC server {} {}:{}", name, host, port);
                    EquivalentAddressGroup addressGroup =
                            new EquivalentAddressGroup(new InetSocketAddress(host, port), Attributes.EMPTY);
                    equivalentAddressGroups.add(addressGroup);
                }
                savedListener.onAddresses(equivalentAddressGroups, Attributes.EMPTY);
            } finally {
                synchronized (AddressChannelNameResolver.this) {
                    resolving = false;
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
        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }
    }

}

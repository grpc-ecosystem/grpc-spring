/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.client.nameresolver;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.annotation.concurrent.GuardedBy;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.NameResolver;
import lombok.extern.slf4j.Slf4j;

/**
 * The DiscoveryClientNameResolver resolves the service hosts and their associated gRPC port using the channel's name
 * and spring's cloud {@link DiscoveryClient}. The ports are extracted from the {@code gRPC.port} metadata.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class DiscoveryClientNameResolver extends NameResolver {
    private final String name;

    private DiscoveryClientResolverFactory factory;

    @GuardedBy("this")
    private Listener listener;
    @GuardedBy("this")
    private boolean shutdown;

    public DiscoveryClientNameResolver(final String name,
            final DiscoveryClientResolverFactory factory) {
        this.name = name;
        this.factory = factory;
    }

    @Override
    public final String getServiceAuthority() {
        return this.name;
    }

    @Override
    public final synchronized void start(final Listener listener) {
        checkState(this.listener == null, "already started");
        this.listener = checkNotNull(listener, "listener");
        factory.registerListener(name, listener);
    }

    @Override
    public final synchronized void refresh() {
        // Heartbeats might happen before the resolver is even started
        // We just ignore that case silently
        // checkState(listener != null, "not started");
        if (this.listener != null && !shutdown) {
            factory.refresh(name);
        }
    }

    @Override
    public void shutdown() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;

        if (this.listener != null) {
            factory.unregisterListener(name, listener);
        }

        this.listener = null;
    }

    @Override
    public String toString() {
        return "DiscoveryClientNameResolver [name=" + this.name + "]";
    }
}

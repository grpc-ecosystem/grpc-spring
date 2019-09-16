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

package net.devh.boot.grpc.server.nameresolver;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.SynchronizationContext;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.config.GrpcServerProperties;

/**
 * A {@link NameResolver} that will always respond with the server's own address.
 */
@Slf4j
public class SelfNameResolver extends NameResolver {

    private final GrpcServerProperties properties;
    private final SharedResourceHolder.Resource<Executor> executorResource;
    private final SynchronizationContext syncContext;

    @GuardedBy("this")
    private Listener listener = null;
    @GuardedBy("this")
    private Executor executor = null;
    @GuardedBy("this")
    private boolean resolving = false;

    /**
     * Creates a self name resolver with the given properties.
     *
     * @param properties The properties to read the server address from.
     * @param args The arguments for the resolver.
     */
    public SelfNameResolver(GrpcServerProperties properties, final Args args) {
        this(properties, args, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
    }

    /**
     * Creates a self name resolver with the given properties.
     *
     * @param properties The properties to read the server address from.
     * @param args The arguments for the resolver.
     * @param executorResource The shared executor resource for channels.
     */
    public SelfNameResolver(GrpcServerProperties properties, final Args args,
            final SharedResourceHolder.Resource<Executor> executorResource) {
        this.properties = requireNonNull(properties, "properties");
        this.executorResource = requireNonNull(executorResource, "executorResource");
        this.syncContext = requireNonNull(args.getSynchronizationContext(), "syncContext");
    }

    @Override
    public String getServiceAuthority() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return getOwnAddressString("localhost");
        }
    }

    @Override
    public final synchronized void start(final Listener listener) {
        checkState(this.listener == null, "already started");
        this.executor = SharedResourceHolder.get(this.executorResource);
        this.listener = checkNotNull(listener, "listener");
        resolve();
    }

    @Override
    public final synchronized void refresh() {
        checkState(this.listener != null, "not started");
        resolve();
    }

    @GuardedBy("this")
    private void resolve() {
        log.debug("Scheduled self resolve");
        if (this.resolving || this.executor == null) {
            return;
        }
        this.resolving = true;
        this.executor.execute(new Resolve(this.listener));
    }

    @Override
    public synchronized void shutdown() {
        this.listener = null;
        if (this.executor != null) {
            this.executor = SharedResourceHolder.release(this.executorResource, this.executor);
        }
    }

    private SocketAddress getOwnAddress() throws SocketException {
        final String address = this.properties.getAddress();
        final int port = this.properties.getPort();
        final SocketAddress target;
        if (GrpcServerProperties.ANY_IP_ADDRESS.equals(address)) {
            target = new InetSocketAddress(port);
        } else {
            target = new InetSocketAddress(InetAddresses.forString(address), port);
        }
        return target;
    }

    private String getOwnAddressString(String fallback) {
        try {
            return getOwnAddress().toString().substring(1);
        } catch (SocketException e) {
            return fallback;
        }
    }

    @Override
    public String toString() {
        return "SelfNameResolver [" + getOwnAddressString("<unavailable>") + "]";
    }

    /**
     * The logic for assigning the own address.
     */
    private final class Resolve implements Runnable {

        private final Listener savedListener;

        /**
         * Creates a new Resolve that stores a snapshot of the relevant states of the resolver.
         *
         * @param listener The listener to send the results to.
         */
        Resolve(final Listener listener) {
            this.savedListener = requireNonNull(listener, "listener");
        }

        @Override
        public void run() {
            try {
                this.savedListener.onAddresses(ImmutableList.of(
                        new EquivalentAddressGroup(getOwnAddress())), Attributes.EMPTY);
            } catch (final Exception e) {
                this.savedListener.onError(Status.UNAVAILABLE
                        .withDescription("Failed to resolve own address").withCause(e));
            } finally {
                SelfNameResolver.this.syncContext.execute(() -> SelfNameResolver.this.resolving = false);
            }
        }

    }

}

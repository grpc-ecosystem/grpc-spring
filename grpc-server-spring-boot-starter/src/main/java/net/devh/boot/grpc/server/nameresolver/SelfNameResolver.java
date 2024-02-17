/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.devh.boot.grpc.server.nameresolver;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;

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
    private final SynchronizationContext syncContext;
    private final SharedResourceHolder.Resource<Executor> executorResource;
    private final boolean usingExecutorResource;

    // Following fields must be accessed from syncContext
    private Executor executor = null;
    private boolean resolving = false;
    // The field must be accessed from syncContext, although the methods on an Listener2 can be called
    // from any thread.
    private Listener2 listener = null;

    /**
     * Creates a self name resolver with the given properties.
     *
     * @param properties The properties to read the server address from.
     * @param args The arguments for the resolver.
     */
    public SelfNameResolver(final GrpcServerProperties properties, final Args args) {
        this(properties, args, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
    }

    /**
     * Creates a self name resolver with the given properties.
     *
     * @param properties The properties to read the server address from.
     * @param args The arguments for the resolver.
     * @param executorResource The shared executor resource for channels.
     */
    public SelfNameResolver(final GrpcServerProperties properties, final Args args,
            final SharedResourceHolder.Resource<Executor> executorResource) {
        this.properties = requireNonNull(properties, "properties");
        this.syncContext = requireNonNull(args.getSynchronizationContext(), "syncContext");
        this.executorResource = requireNonNull(executorResource, "executorResource");
        this.executor = args.getOffloadExecutor();
        this.usingExecutorResource = this.executor == null;
    }

    @Override
    public String getServiceAuthority() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            return getOwnAddressString("localhost");
        }
    }

    @Override
    public final void start(final Listener2 listener) {
        checkState(this.listener == null, "already started");
        this.listener = checkNotNull(listener, "listener");
        if (this.usingExecutorResource) {
            this.executor = SharedResourceHolder.get(this.executorResource);
        }
        resolve();
    }

    @Override
    public final void refresh() {
        checkState(this.listener != null, "not started");
        resolve();
    }

    private void resolve() {
        log.debug("Scheduled self resolve");
        if (this.resolving || this.executor == null) {
            return;
        }
        this.resolving = true;
        this.executor.execute(new Resolve(this.listener));
    }

    @Override
    public void shutdown() {
        this.listener = null;
        if (this.executor != null && this.usingExecutorResource) {
            this.executor = SharedResourceHolder.release(this.executorResource, this.executor);
        }
    }

    private SocketAddress getOwnAddress() {
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

    private String getOwnAddressString(final String fallback) {
        try {
            return getOwnAddress().toString().substring(1);
        } catch (final IllegalArgumentException e) {
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

        private final Listener2 savedListener;

        /**
         * Creates a new Resolve that stores a snapshot of the relevant states of the resolver.
         *
         * @param listener The listener to send the results to.
         */
        Resolve(final Listener2 listener) {
            this.savedListener = requireNonNull(listener, "listener");
        }

        @Override
        public void run() {
            try {
                this.savedListener.onResult(ResolutionResult.newBuilder()
                        .setAddresses(ImmutableList.of(
                                new EquivalentAddressGroup(getOwnAddress())))
                        .build());
            } catch (final Exception e) {
                this.savedListener.onError(Status.UNAVAILABLE
                        .withDescription("Failed to resolve own address").withCause(e));
            } finally {
                SelfNameResolver.this.syncContext.execute(() -> SelfNameResolver.this.resolving = false);
            }
        }

    }

}

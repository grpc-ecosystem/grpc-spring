/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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
import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.SynchronizationContext;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.util.GrpcUtils;

/**
 * The DiscoveryClientNameResolver resolves the service hosts and their associated gRPC port using the channel's name
 * and spring's cloud {@link DiscoveryClient}. The ports are extracted from the {@code gRPC_port} metadata.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class DiscoveryClientNameResolver extends NameResolver {

    @Deprecated
    private static final String LEGACY_CLOUD_DISCOVERY_METADATA_PORT = "gRPC.port";
    private static final List<ServiceInstance> KEEP_PREVIOUS = null;

    private final String name;
    private final DiscoveryClient client;
    private final SynchronizationContext syncContext;
    private final Runnable externalCleaner;
    private final SharedResourceHolder.Resource<Executor> executorResource;
    private final boolean usingExecutorResource;

    // The field must be accessed from syncContext, although the methods on an Listener2 can be called
    // from any thread.
    private Listener2 listener;
    // Following fields must be accessed from syncContext
    private Executor executor;
    private boolean resolving;
    private List<ServiceInstance> instanceList = Lists.newArrayList();

    /**
     * Creates a new DiscoveryClientNameResolver.
     *
     * @param name The name of the service to look up.
     * @param client The client used to look up the service addresses.
     * @param args The name resolver args.
     * @param executorResource The executor resource.
     * @param externalCleaner The optional cleaner used during {@link #shutdown()}
     */
    public DiscoveryClientNameResolver(final String name, final DiscoveryClient client, final Args args,
            final SharedResourceHolder.Resource<Executor> executorResource, final Runnable externalCleaner) {
        this.name = name;
        this.client = client;
        this.syncContext = requireNonNull(args.getSynchronizationContext(), "syncContext");
        this.externalCleaner = externalCleaner;
        this.executor = args.getOffloadExecutor();
        this.usingExecutorResource = this.executor == null;
        this.executorResource = executorResource;
    }

    @Override
    public final String getServiceAuthority() {
        return this.name;
    }

    @Override
    public void start(final Listener2 listener) {
        checkState(this.listener == null, "already started");
        if (this.usingExecutorResource) {
            this.executor = SharedResourceHolder.get(this.executorResource);
        }
        this.listener = checkNotNull(listener, "listener");
        resolve();
    }

    @Override
    public void refresh() {
        checkState(this.listener != null, "not started");
        resolve();
    }

    /**
     * Triggers a refresh on the listener from non-grpc threads. This method can safely be called, even if the listener
     * hasn't been started yet.
     *
     * @see #refresh()
     */
    public void refreshFromExternal() {
        this.syncContext.execute(() -> {
            if (this.listener != null) {
                resolve();
            }
        });
    }

    private void resolve() {
        log.debug("Scheduled resolve for {}", this.name);
        if (this.resolving) {
            return;
        }
        this.resolving = true;
        this.executor.execute(new Resolve(this.listener, this.instanceList));
    }

    @Override
    public void shutdown() {
        this.listener = null;
        if (this.executor != null && this.usingExecutorResource) {
            this.executor = SharedResourceHolder.release(this.executorResource, this.executor);
        }
        this.instanceList = Lists.newArrayList();
        if (this.externalCleaner != null) {
            this.externalCleaner.run();
        }
    }

    @Override
    public String toString() {
        return "DiscoveryClientNameResolver [name=" + this.name + ", discoveryClient=" + this.client + "]";
    }

    /**
     * The logic for updating the gRPC server list using a discovery client.
     */
    private final class Resolve implements Runnable {

        private final Listener2 savedListener;
        private final List<ServiceInstance> savedInstanceList;

        /**
         * Creates a new Resolve that stores a snapshot of the relevant states of the resolver.
         *
         * @param listener The listener to send the results to.
         * @param instanceList The current server instance list.
         */
        Resolve(final Listener2 listener, final List<ServiceInstance> instanceList) {
            this.savedListener = requireNonNull(listener, "listener");
            this.savedInstanceList = requireNonNull(instanceList, "instanceList");
        }

        @Override
        public void run() {
            final AtomicReference<List<ServiceInstance>> resultContainer = new AtomicReference<>();
            try {
                resultContainer.set(resolveInternal());
            } catch (final Exception e) {
                this.savedListener.onError(Status.UNAVAILABLE.withCause(e)
                        .withDescription("Failed to update server list for " + DiscoveryClientNameResolver.this.name));
                resultContainer.set(Lists.newArrayList());
            } finally {
                DiscoveryClientNameResolver.this.syncContext.execute(() -> {
                    DiscoveryClientNameResolver.this.resolving = false;
                    final List<ServiceInstance> result = resultContainer.get();
                    if (result != KEEP_PREVIOUS && DiscoveryClientNameResolver.this.listener != null) {
                        DiscoveryClientNameResolver.this.instanceList = result;
                    }
                });
            }
        }

        /**
         * Do the actual update checks and resolving logic.
         *
         * @return The new service instance list that is used to connect to the gRPC server or null if the old ones
         *         should be used.
         */
        private List<ServiceInstance> resolveInternal() {
            final String name = DiscoveryClientNameResolver.this.name;
            final List<ServiceInstance> newInstanceList =
                    DiscoveryClientNameResolver.this.client.getInstances(name);
            log.debug("Got {} candidate servers for {}", newInstanceList.size(), name);
            if (CollectionUtils.isEmpty(newInstanceList)) {
                log.error("No servers found for {}", name);
                this.savedListener.onError(Status.UNAVAILABLE.withDescription("No servers found for " + name));
                return Lists.newArrayList();
            }
            if (!needsToUpdateConnections(newInstanceList)) {
                log.debug("Nothing has changed... skipping update for {}", name);
                return KEEP_PREVIOUS;
            }
            log.debug("Ready to update server list for {}", name);
            final List<EquivalentAddressGroup> targets = Lists.newArrayList();
            for (final ServiceInstance instance : newInstanceList) {
                final int port = getGRPCPort(instance);
                log.debug("Found gRPC server {}:{} for {}", instance.getHost(), port, name);
                targets.add(new EquivalentAddressGroup(
                        new InetSocketAddress(instance.getHost(), port), Attributes.EMPTY));
            }
            if (targets.isEmpty()) {
                log.error("None of the servers for {} specified a gRPC port", name);
                this.savedListener.onError(Status.UNAVAILABLE
                        .withDescription("None of the servers for " + name + " specified a gRPC port"));
                return Lists.newArrayList();
            } else {
                this.savedListener.onResult(ResolutionResult.newBuilder()
                        .setAddresses(targets)
                        .build());
                log.info("Done updating server list for {}", name);
                return newInstanceList;
            }
        }

        /**
         * Extracts the gRPC server port from the given service instance.
         *
         * @param instance The instance to extract the port from.
         * @return The gRPC server port.
         * @throws IllegalArgumentException If the specified port definition couldn't be parsed.
         */
        private int getGRPCPort(final ServiceInstance instance) {
            final Map<String, String> metadata = instance.getMetadata();
            if (metadata == null) {
                return instance.getPort();
            }
            String portString = metadata.get(GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT);
            if (portString == null) {
                portString = metadata.get(LEGACY_CLOUD_DISCOVERY_METADATA_PORT);
                if (portString == null) {
                    return instance.getPort();
                } else {
                    log.warn("Found legacy grpc port metadata '{}' for client '{}' use '{}' instead",
                            LEGACY_CLOUD_DISCOVERY_METADATA_PORT, DiscoveryClientNameResolver.this.name,
                            GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT);
                }
            }
            try {
                return Integer.parseInt(portString);
            } catch (final NumberFormatException e) {
                // TODO: How to handle this case?
                throw new IllegalArgumentException("Failed to parse gRPC port information from: " + instance, e);
            }
        }

        /**
         * Checks whether this instance should update its connections.
         *
         * @param newInstanceList The new instances that should be compared to the stored ones.
         * @return True, if the given instance list contains different entries than the stored ones.
         */
        private boolean needsToUpdateConnections(final List<ServiceInstance> newInstanceList) {
            if (this.savedInstanceList.size() != newInstanceList.size()) {
                return true;
            }
            for (final ServiceInstance instance : this.savedInstanceList) {
                final int port = getGRPCPort(instance);
                boolean isSame = false;
                for (final ServiceInstance newInstance : newInstanceList) {
                    final int newPort = getGRPCPort(newInstance);
                    if (newInstance.getHost().equals(instance.getHost())
                            && port == newPort) {
                        isSame = true;
                        break;
                    }
                }
                if (!isSame) {
                    return true;
                }
            }
            return false;
        }

    }

}

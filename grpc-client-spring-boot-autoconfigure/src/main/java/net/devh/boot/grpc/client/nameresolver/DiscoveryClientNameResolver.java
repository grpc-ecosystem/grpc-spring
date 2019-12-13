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
import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.GuardedBy;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableList;
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
 * and spring's cloud {@link DiscoveryClient}. The ports are extracted from the {@code gRPC.port} metadata.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class DiscoveryClientNameResolver extends NameResolver {

    private static final List<ServiceInstance> KEEP_PREVIOUS = null;
    private static final List<ServiceInstance> NO_SERVERS_AVAILABLE = ImmutableList.of();

    private final String name;
    private final DiscoveryClient client;
    private final SharedResourceHolder.Resource<Executor> executorResource;
    private final SynchronizationContext syncContext;

    @GuardedBy("this")
    private Listener listener;
    @GuardedBy("this")
    private Executor executor;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private List<ServiceInstance> instanceList = NO_SERVERS_AVAILABLE;

    public DiscoveryClientNameResolver(final String name, final DiscoveryClient client, final Args args,
            final SharedResourceHolder.Resource<Executor> executorResource) {
        this.name = name;
        this.client = client;
        this.executorResource = executorResource;
        this.syncContext = requireNonNull(args.getSynchronizationContext(), "syncContext");
    }

    @Override
    public final String getServiceAuthority() {
        return this.name;
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
        // Heartbeats might happen before the resolver is even started
        // We just ignore that case silently
        // checkState(listener != null, "not started");
        if (this.listener != null) {
            resolve();
        }
    }

    @GuardedBy("this")
    private void resolve() {
        log.debug("Scheduled resolve for {}", this.name);
        if (this.resolving || this.shutdown) {
            return;
        }
        this.resolving = true;
        this.executor.execute(new Resolve(this.listener, this.instanceList));
    }

    @Override
    public void shutdown() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        if (this.executor != null) {
            this.executor = SharedResourceHolder.release(this.executorResource, this.executor);
        }
        this.listener = null;
        this.instanceList = NO_SERVERS_AVAILABLE;
    }

    @Override
    public String toString() {
        return "DiscoveryClientNameResolver [name=" + this.name + ", discoveryClient=" + this.client + "]";
    }

    /**
     * The logic for updating the gRPC server list using a discovery client.
     */
    private final class Resolve implements Runnable {

        private final Listener savedListener;
        private final List<ServiceInstance> savedInstanceList;

        /**
         * Creates a new Resolve that stores a snapshot of the relevant states of the resolver.
         *
         * @param listener The listener to send the results to.
         * @param instanceList The current server instance list.
         */
        Resolve(final Listener listener, final List<ServiceInstance> instanceList) {
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
                resultContainer.set(NO_SERVERS_AVAILABLE);
            } finally {
                DiscoveryClientNameResolver.this.syncContext.execute(() -> {
                    DiscoveryClientNameResolver.this.resolving = false;
                    final List<ServiceInstance> result = resultContainer.get();
                    if (result != KEEP_PREVIOUS && !DiscoveryClientNameResolver.this.shutdown) {
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
                return NO_SERVERS_AVAILABLE;
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
                return NO_SERVERS_AVAILABLE;
            } else {
                this.savedListener.onAddresses(targets, Attributes.EMPTY);
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
            final String portString = metadata.get(GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT);
            if (portString == null) {
                return instance.getPort();
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

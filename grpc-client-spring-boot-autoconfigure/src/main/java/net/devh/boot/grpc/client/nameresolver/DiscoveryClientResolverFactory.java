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

import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.errorprone.annotations.concurrent.GuardedBy;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolver.Listener;
import io.grpc.NameResolver.Listener2;
import io.grpc.NameResolver.ResolutionResult;
import io.grpc.NameResolverProvider;
import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.slf4j.Slf4j;

/**
 * A name resolver factory that will create a {@link DiscoveryClientNameResolver} based on the target uri.
 *
 * @author Michael (yidongnan@gmail.com)
 */
// Do not add this to the NameResolverProvider service loader list
@Slf4j
public class DiscoveryClientResolverFactory extends NameResolverProvider {

    private static final ImmutableList<ServiceInstance> KEEP_PREVIOUS = null;
    private static final ImmutableList<ServiceInstance> NO_SERVERS_FOUND = ImmutableList.of();
    private static final ImmutableList<ServiceInstance> NOT_INITIALIZED = ImmutableList.of();

    /**
     * The constant containing the scheme that will be used by this factory.
     */
    public static final String DISCOVERY_SCHEME = "discovery";

    private final HeartbeatMonitor monitor = new HeartbeatMonitor();

    private final DiscoveryClient client;
    private final ExecutorService executor;

    // Listeners by service name
    @GuardedBy("this")
    private final Multimap<String, Listener2> listenersByName = LinkedHashMultimap.create();
    // The resolved service instances for a given name
    @GuardedBy("this")
    private final Map<String, ImmutableList<ServiceInstance>> serviceInstanceByName = new HashMap<>();
    // The currently resolving service names
    @GuardedBy("this")
    private final Set<String> resolvingServices = new HashSet<>();

    /**
     * Creates a new discovery client based name resolver factory. This constructor uses a cached
     * {@link ExecutorService} that creates demon threads with a {@code "grpc-discovery-resolver-"} prefix.
     *
     * @param client The client to use for the address discovery.
     * @see #DiscoveryClientResolverFactory(DiscoveryClient, ExecutorService)
     */
    public DiscoveryClientResolverFactory(final DiscoveryClient client) {
        this.client = requireNonNull(client, "client");
        final CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("grpc-discovery-resolver-");
        threadFactory.setDaemon(true);
        this.executor = Executors.newCachedThreadPool(threadFactory);
    }

    /**
     * Creates a new discovery client based name resolver factory.
     *
     * @param client The client to use for the address discovery.
     * @param executor The executor used to resolve the service names. Will be {@link ExecutorService#shutdownNow()
     *        shutdown} when {@link #destroy()} is called.
     */
    public DiscoveryClientResolverFactory(final DiscoveryClient client, final ExecutorService executor) {
        this.client = requireNonNull(client, "client");
        this.executor = requireNonNull(executor, "executor");
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(final URI targetUri, final NameResolver.Args args) {
        if (DISCOVERY_SCHEME.equals(targetUri.getScheme())) {
            final String serviceName = targetUri.getPath();
            if (serviceName == null || serviceName.length() <= 1 || !serviceName.startsWith("/")) {
                throw new IllegalArgumentException("Incorrectly formatted target uri; "
                        + "expected: '" + DISCOVERY_SCHEME + ":[//]/<service-name>'; "
                        + "but was '" + targetUri.toString() + "'");
            }
            return new DiscoveryClientNameResolver(serviceName.substring(1), this);
        }
        return null;
    }

    @Override
    public String getDefaultScheme() {
        return DISCOVERY_SCHEME;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 6; // More important than DNS
    }

    /**
     * Registers the given listener in this factory for automated updates.
     *
     * @param name The service name of the listener.
     * @param listener The listener to register.
     */
    public final synchronized void registerListener(final String name, final Listener2 listener) {
        requireNonNull(name, "name");
        requireNonNull(listener, "listener");

        this.listenersByName.put(name, listener);
        final List<ServiceInstance> instances = this.serviceInstanceByName.computeIfAbsent(name, n -> NOT_INITIALIZED);

        boolean force = false;

        if (instances.isEmpty()) {
            // no instance has GRPC port, clean cached instances to force notifying all listeners.
            force = instances != NOT_INITIALIZED;
        } else {
            // notify listener with cached instance first for latency, in most case it improves a lot.
            listener.onResult(convert(name, instances));
        }

        refresh(name, force);
    }

    /**
     * Unregisters the given listener from this factory, excluding it from further updates.
     *
     * @param name The service name of the listener.
     * @param listener The listener to unregister.
     */
    public final synchronized void unregisterListener(final String name, final Listener2 listener) {
        this.listenersByName.remove(name, listener);
    }

    /**
     * Triggers a refresh for all known service names.
     *
     * @param force Whether to force a new resolution even if one is already in progress.
     */
    public synchronized void refreshAll(final boolean force) {
        for (final String name : this.listenersByName.keySet()) {
            refresh(name, force);
        }
    }

    /**
     * Triggers a refresh for the given service name.
     *
     * @param name The service name to trigger the refresh for.
     * @param force Whether to force a new resolution even if one is already in progress.
     */
    public final synchronized void refresh(final String name, final boolean force) {
        // no resolver is running with this service name.
        if (!this.listenersByName.containsKey(name)) {
            log.debug("No listener for {} -> Skipping", name);
            return;
        }

        // exit when resolving but not a force refresh
        if (!this.resolvingServices.add(name) && !force) {
            log.debug("Resolution already in progress for {} -> Skipping", name);
            return;
        }

        this.executor.submit(new Resolve(name, this.serviceInstanceByName.get(name)));
    }

    /**
     * Triggers a refresh of the registered name resolvers.
     *
     * @param event The event that triggered the update.
     */
    @EventListener(HeartbeatEvent.class)
    public void heartbeat(final HeartbeatEvent event) {
        if (this.monitor.update(event.getValue())) {
            refreshAll(false);
        }
    }

    /**
     * Cleans up the name resolvers.
     */
    @PreDestroy
    public synchronized void destroy() {
        this.executor.shutdownNow();
        try {
            this.executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // safe to clean all
        this.listenersByName.clear();
        this.serviceInstanceByName.clear();
        this.resolvingServices.clear();
    }

    @Override
    public String toString() {
        return "DiscoveryClientResolverFactory [scheme=" + getDefaultScheme() +
                ", discoveryClient=" + this.client + "]";
    }

    // Utility methods

    /**
     * Converts the given server instances to grpc's {@link EquivalentAddressGroup}s.
     *
     * @param name The name of the service these addresses belong to.
     * @param instanceList The list of service instance to convert.
     * @return The converted list of instances.
     */
    private static ResolutionResult convert(
            final String name,
            final Collection<ServiceInstance> instanceList) {
        final List<EquivalentAddressGroup> targets = new ArrayList<>();

        for (final ServiceInstance instance : instanceList) {
            try {
                final int port = getGRPCPort(instance);
                log.debug("Found gRPC server {}:{} for {}", instance.getHost(), port, name);
                targets.add(new EquivalentAddressGroup(
                        new InetSocketAddress(instance.getHost(), port), Attributes.EMPTY));
            } catch (final IllegalArgumentException e) {
                log.warn("Failed to parse server info from '{}' -> Skipping entry", instance, e);
            }
        }

        return ResolutionResult.newBuilder()
                .setAddresses(targets)
                .build();
    }

    /**
     * Extracts the gRPC server port from the given service instance.
     *
     * @param instance The instance to extract the port from.
     * @return The gRPC server port.
     * @throws IllegalArgumentException If the specified port definition couldn't be parsed.
     */
    private static int getGRPCPort(final ServiceInstance instance) {
        final Map<String, String> metadata = instance.getMetadata();
        if (metadata == null) {
            return instance.getPort();
        }
        final String portString = metadata.get("gRPC.port");
        if (portString == null) {
            return instance.getPort();
        }
        try {
            return Integer.parseInt(portString);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse gRPC port information from: " + instance, e);
        }
    }

    /**
     * The logic for updating the gRPC server list using a discovery client.
     */
    private final class Resolve implements Runnable {

        private final String name;
        private final ImmutableList<ServiceInstance> savedInstanceList;

        /**
         * Creates a new Resolve that stores a snapshot of the relevant states of the resolver.
         *
         * @param name The service name to resolve.
         * @param instanceList The current server instance list. Used to determine whether the server list needs to be
         *        updated.
         */
        Resolve(final String name, final ImmutableList<ServiceInstance> instanceList) {
            this.name = requireNonNull(name, "name");
            this.savedInstanceList = requireNonNull(instanceList, "instanceList");
        }

        @Override
        public void run() {
            try {
                resolveInternal();
            } catch (final StatusException e) {
                log.error("Could not update server list for {}", this.name, e);
                onError(e.getStatus());
            } catch (final Exception e) {
                log.error("Could not update server list for {}", this.name, e);
                onError(Status.UNAVAILABLE.withCause(e)
                        .withDescription("Failed to update server list for " + this.name));
            }
        }

        /**
         * Do the actual update checks and resolving logic.
         *
         * @throws StatusException If something went wrong during the resolution.
         */
        private void resolveInternal() throws StatusException {
            final List<ServiceInstance> newInstanceList = client.getInstances(this.name);
            log.debug("Got {} candidate servers for {}", newInstanceList.size(), this.name);
            if (newInstanceList.isEmpty()) {
                throw Status.UNAVAILABLE
                        .withDescription("No servers found for " + this.name)
                        .asException();
            }
            if (!needsToUpdateConnections(newInstanceList)) {
                log.debug("Nothing has changed... skipping update for {}", this.name);
                onAddresses(KEEP_PREVIOUS, null);
                return;
            }
            log.debug("Ready to update server list for {}", this.name);
            final ResolutionResult result = convert(this.name, newInstanceList);
            if (result.getAddresses().isEmpty()) {
                throw Status.UNAVAILABLE
                        .withDescription("None of the servers for " + this.name + " specified a gRPC port")
                        .asException();
            } else {
                onAddresses(newInstanceList, result);
                log.info("Done updating server list for {}", this.name);
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

        /**
         * Updates all registered listeners with the given server addresses.
         *
         * @param serviceList The service list used to determine the targets.
         * @param result The target addresses to connect to.
         */
        private void onAddresses(
                final List<ServiceInstance> serviceList,
                final ResolutionResult result) {
            if (Thread.currentThread().isInterrupted()) {
                return; // Cancelled
            }

            Collection<Listener2> listeners = ImmutableSet.of();

            // 1. Make a copy of listeners, any listener registered before here will be notified here.
            synchronized (DiscoveryClientResolverFactory.this) {
                resolvingServices.remove(this.name);
                if (serviceList != KEEP_PREVIOUS) {
                    serviceInstanceByName.put(this.name, ImmutableList.copyOf(serviceList));
                    listeners = ImmutableSet.copyOf(listenersByName.get(this.name));
                }
            }
            // 2. Since serviceInstanceByName was updated, any new listener will be notified when registering.
            for (final Listener2 listener : listeners) {
                listener.onResult(result);
            }
        }

        /**
         * Updates all registered listeners with the given error {@link Status}.
         *
         * @param status The error status to publish.
         */
        private void onError(final Status status) {
            if (Thread.currentThread().isInterrupted()) {
                return; // Cancelled
            }

            Collection<Listener2> listeners;

            synchronized (DiscoveryClientResolverFactory.this) {
                resolvingServices.remove(this.name);
                serviceInstanceByName.put(this.name, NO_SERVERS_FOUND);
                listeners = ImmutableSet.copyOf(listenersByName.get(this.name));
            }

            for (final Listener listener : listeners) {
                listener.onError(status);
            }
        }

    }
}

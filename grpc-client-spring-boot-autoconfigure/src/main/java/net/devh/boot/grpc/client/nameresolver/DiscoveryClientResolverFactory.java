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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolver.Listener;
import io.grpc.NameResolverProvider;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

/**
 * A name resolver factory that will create a {@link DiscoveryClientNameResolver} based on the target uri.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
// Do not add this to the NameResolverProvider service loader list
@Slf4j
public class DiscoveryClientResolverFactory extends NameResolverProvider {
    private static final List<ServiceInstance> KEEP_PREVIOUS = null;

    /**
     * The constant containing the scheme that will be used by this factory.
     */
    public static final String DISCOVERY_SCHEME = "discovery";

    private final HeartbeatMonitor monitor = new HeartbeatMonitor();

    private final DiscoveryClient client;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, Set<Listener>> listenerMap = new HashMap<>();
    private final Map<String, List<ServiceInstance>> serviceInstanceMap = new HashMap<>();
    private final Map<String, Future<List<ServiceInstance>>> discoverClientTasks = new HashMap<>();

    /**
     * Creates a new discovery client based name resolver factory.
     *
     * @param client The client to use for the address discovery.
     */
    public DiscoveryClientResolverFactory(final DiscoveryClient client) {
        this.client = requireNonNull(client, "client");
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

    public final synchronized void registerListener(String name, Listener listener) {
        Preconditions.checkState(listener != null, "invalid listener");

        listenerMap.computeIfAbsent(name, n -> Sets.newHashSet()).add(listener);

        List<ServiceInstance> instances = serviceInstanceMap.get(name);

        // notify listener with cached instance first for latency, in most case it improves a lot.
        if (instances != null) {
            List<EquivalentAddressGroup> targets = convert(name, instances);

            // no instance has GRPC port, clean cached instances to force notifying all listeners.
            if (targets.isEmpty()) {
                serviceInstanceMap.remove(name);
            } else {
                listener.onAddresses(targets, Attributes.EMPTY);
            }
        }

        refresh(name);
    }

    public final synchronized void unregisterListener(String name, Listener listener) {
        Set<Listener> listeners = listenerMap.get(name);

        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private synchronized void refresh() {
        for (String name : listenerMap.keySet()) {
            refresh(name);
        }
    }

    private boolean resolving(Future<List<ServiceInstance>> future) {
        return future != null && !future.isDone();
    }

    private List<ServiceInstance> getResolveResult(Future<List<ServiceInstance>> future) {
        try {
            if (future != null && future.isDone()) {
                return future.get();
            }
        } catch (ExecutionException | InterruptedException ignored) {
        }

        return KEEP_PREVIOUS;
    }

    private boolean forceRefresh(String name) {
        return serviceInstanceMap.get(name) == null;
    }

    public final synchronized void refresh(String name) {
        Future<List<ServiceInstance>> future = discoverClientTasks.get(name);

        // no resolver is running with this service name.
        if (CollectionUtils.isEmpty(listenerMap.get(name))) {
            return;
        }

        // exit when resolving but not a force refresh
        if (resolving(future) && !forceRefresh(name)) {
            return;
        }

        // update cached instances when not a force refresh.
        if (!forceRefresh(name)) {
            List<ServiceInstance> instances = getResolveResult(future);

            if (instances != KEEP_PREVIOUS) {
                serviceInstanceMap.put(name, instances);
            }
        }

        discoverClientTasks.put(name,
                executor.submit(new Resolve(name, Sets.newHashSet(listenerMap.get(name)),
                        Lists.newArrayList(
                                serviceInstanceMap.computeIfAbsent(name, n -> Lists.newArrayList())))));
    }

    private final class Resolve implements Callable<List<ServiceInstance>> {

        private final String name;
        private final Set<Listener> savedListenerList;
        private final List<ServiceInstance> savedInstanceList;

        /**
         * Creates a new Resolve that stores a snapshot of the relevant states of the resolver.
         *
         * @param listenerList The listener to send the results to.
         * @param instanceList The current server instance list.
         */
        Resolve(final String name, final Set<Listener> listenerList, final List<ServiceInstance> instanceList) {
            this.name = requireNonNull(name, "name");
            this.savedListenerList = requireNonNull(listenerList, "listenerList");
            this.savedInstanceList = requireNonNull(instanceList, "instanceList");
        }

        @Override
        public List<ServiceInstance> call() {
            try {
                return resolveInternal();
            } catch (final Exception e) {
                notifyStatus(Status.UNAVAILABLE.withCause(e)
                        .withDescription("Failed to update server list for " + name));
            }

            return KEEP_PREVIOUS;
        }

        private void notifyAddresses(List<EquivalentAddressGroup> targets, Attributes attributes) {
            for (Listener listener : savedListenerList) {
                try {
                    listener.onAddresses(targets, attributes);
                } catch (Exception ignored) {
                }
            }
        }

        private void notifyStatus(Status status) {
            for (Listener listener : savedListenerList) {
                try {
                    listener.onError(status);
                } catch (Exception ignored) {
                }
            }
        }

        /**
         * Do the actual update checks and resolving logic.
         *
         * @return The new service instance list that is used to connect to the gRPC server or null if the old ones
         *         should be used.
         */
        private List<ServiceInstance> resolveInternal() {
            final List<ServiceInstance> newInstanceList =
                    DiscoveryClientResolverFactory.this.client.getInstances(name);
            log.debug("Got {} candidate servers for {}", newInstanceList.size(), name);
            if (CollectionUtils.isEmpty(newInstanceList)) {
                log.error("No servers found for {}", name);
                notifyStatus(Status.UNAVAILABLE.withDescription("No servers found for " + name));
                return Lists.newArrayList();
            }
            if (!needsToUpdateConnections(newInstanceList)) {
                log.debug("Nothing has changed... skipping update for {}", name);
                return KEEP_PREVIOUS;
            }
            log.debug("Ready to update server list for {}", name);
            final List<EquivalentAddressGroup> targets = convert(name, newInstanceList);
            if (targets.isEmpty()) {
                log.error("None of the servers for {} specified a gRPC port", name);
                notifyStatus(Status.UNAVAILABLE
                        .withDescription("None of the servers for " + name + " specified a gRPC port"));
                return Lists.newArrayList();
            } else {
                notifyAddresses(targets, Attributes.EMPTY);
                log.info("Done updating server list for {}", name);
                return newInstanceList;
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

    private List<EquivalentAddressGroup> convert(String name, List<ServiceInstance> newInstanceList) {
        final List<EquivalentAddressGroup> targets = Lists.newArrayList();

        for (final ServiceInstance instance : newInstanceList) {
            try {
                final int port = getGRPCPort(instance);
                log.debug("Found gRPC server {}:{} for {}", instance.getHost(), port, name);
                targets.add(new EquivalentAddressGroup(
                        new InetSocketAddress(instance.getHost(), port), Attributes.EMPTY));
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage(), e);
            }
        }

        return targets;
    }

    /**
     * Triggers a refresh of the registered name resolvers.
     *
     * @param event The event that triggered the update.
     */
    @EventListener(HeartbeatEvent.class)
    public void heartbeat(final HeartbeatEvent event) {
        if (this.monitor.update(event.getValue())) {
            refresh();
        }
    }

    /**
     * Cleans up the name resolvers.
     */
    @PreDestroy
    public void destroy() {
        // interrupt
        for (Future<?> task : discoverClientTasks.values()) {
            task.cancel(true);
        }

        // wait for complete
        for (Future<?> task : discoverClientTasks.values()) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }

        // safe to clean all
        listenerMap.clear();
    }

    @Override
    public String toString() {
        return "DiscoveryClientResolverFactory [scheme=" + getDefaultScheme() +
                ", discoveryClient=" + this.client + "]";
    }

}

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

package net.devh.boot.grpc.client.nameresolver;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.client.nameresolver.DiscoveryClientResolverFactory.DISCOVERY_INSTANCE_ID_KEY;
import static net.devh.boot.grpc.client.nameresolver.DiscoveryClientResolverFactory.DISCOVERY_SERVICE_NAME_KEY;
import static net.devh.boot.grpc.common.util.GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT;
import static net.devh.boot.grpc.common.util.GrpcUtils.CLOUD_DISCOVERY_METADATA_SERVICE_CONFIG;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.grpc.Attributes;
import io.grpc.Attributes.Builder;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.SynchronizationContext;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;

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
    private static final Gson GSON = new Gson();

    private final String name;
    private final DiscoveryClient client;
    private final SynchronizationContext syncContext;
    private final Consumer<DiscoveryClientNameResolver> shutdownHook;
    private final SharedResourceHolder.Resource<Executor> executorResource;
    private final boolean usingExecutorResource;
    private final ServiceConfigParser serviceConfigParser;

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
     * @param shutdownHook The optional cleaner used during {@link #shutdown()}
     */
    public DiscoveryClientNameResolver(final String name, final DiscoveryClient client, final Args args,
            final SharedResourceHolder.Resource<Executor> executorResource,
            final Consumer<DiscoveryClientNameResolver> shutdownHook) {
        this.name = name;
        this.client = client;
        this.syncContext = requireNonNull(args.getSynchronizationContext(), "syncContext");
        this.shutdownHook = shutdownHook;
        this.executor = args.getOffloadExecutor();
        this.usingExecutorResource = this.executor == null;
        this.executorResource = executorResource;
        this.serviceConfigParser = args.getServiceConfigParser();
    }

    /**
     * Gets the name of the service to get the instances of.
     *
     * @return The name associated with this resolver.
     */
    protected final String getName() {
        return this.name;
    }

    /**
     * Checks whether this resolver is active. E.g. {@code #start} has been called, but not {@code #shutdown()}.
     *
     * @return True, if there is a listener attached. False, otherwise.
     */
    protected final boolean isActive() {
        return this.listener != null;
    }

    @Override
    public final String getServiceAuthority() {
        return this.name;
    }

    @Override
    public void start(final Listener2 listener) {
        checkState(!isActive(), "already started");
        if (this.usingExecutorResource) {
            this.executor = SharedResourceHolder.get(this.executorResource);
        }
        this.listener = checkNotNull(listener, "listener");
        resolve();
    }

    @Override
    public void refresh() {
        checkState(isActive(), "not started");
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
            if (isActive()) {
                resolve();
            }
        });
    }

    /**
     * Discovers matching service instances. Can be overwritten to apply some custom filtering.
     *
     * @return A list of service instances to use.
     */
    protected List<ServiceInstance> discoverServers() {
        return this.client.getInstances(this.name);
    }

    /**
     * Extracts the gRPC server port from the given service instance. Can be overwritten for a custom port mapping.
     *
     * @param instance The instance to extract the port from.
     * @return The gRPC server port.
     * @throws IllegalArgumentException If the specified port definition couldn't be parsed.
     */
    protected int getGrpcPort(final ServiceInstance instance) {
        final Map<String, String> metadata = instance.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return instance.getPort();
        }
        String portString = metadata.get(CLOUD_DISCOVERY_METADATA_PORT);
        if (portString == null) {
            portString = metadata.get(LEGACY_CLOUD_DISCOVERY_METADATA_PORT);
            if (portString == null) {
                return instance.getPort();
            } else {
                log.warn("Found legacy grpc port metadata '{}' for client '{}' use '{}' instead",
                        LEGACY_CLOUD_DISCOVERY_METADATA_PORT, getName(), CLOUD_DISCOVERY_METADATA_PORT);
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
     * Extracts and parse gRPC service config from the given service instances.
     *
     * @param instances The list of instances to extract the service config from.
     * @return Parsed gRPC service config or null.
     */
    private ConfigOrError resolveServiceConfig(List<ServiceInstance> instances) {
        final String serviceConfig = getServiceConfig(instances);
        if (serviceConfig == null) {
            return null;
        }
        log.debug("Found service config for {}", getName());
        if (log.isTraceEnabled()) {
            // This is to avoid blowing log into several lines if newlines present in service config string.
            final String logStr = serviceConfig.replace("\r", "\\r").replace("\n", "\\n");
            log.trace("Service config for {}: {}", getName(), logStr);
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, ?> parsedServiceConfig = GSON.fromJson(serviceConfig, Map.class);
            return serviceConfigParser.parseServiceConfig(parsedServiceConfig);
        } catch (JsonSyntaxException e) {
            return ConfigOrError.fromError(
                    Status.UNKNOWN
                            .withDescription("Failed to parse grpc service config")
                            .withCause(e));
        }
    }

    /**
     * Extracts the gRPC service config string from the given service instances.
     *
     * @param instances The list of instances to extract the service config from.
     * @return The gRPC service config or null.
     */
    protected String getServiceConfig(final List<ServiceInstance> instances) {
        for (final ServiceInstance inst : instances) {
            final Map<String, String> metadata = inst.getMetadata();
            if (metadata == null || metadata.isEmpty()) {
                continue;
            }
            final String metaValue = metadata.get(CLOUD_DISCOVERY_METADATA_SERVICE_CONFIG);
            if (metaValue != null && !metaValue.isEmpty()) {
                return metaValue;
            }
        }
        return null;
    }

    /**
     * Gets the attributes from the service instance for later use in a load balancer. Can be overwritten to convert
     * custom attributes.
     *
     * @param serviceInstance The service instance to get them from.
     * @return The newly created attributes for the given instance.
     */
    protected Attributes getAttributes(final ServiceInstance serviceInstance) {
        final Builder builder = Attributes.newBuilder();
        builder.set(DISCOVERY_SERVICE_NAME_KEY, this.name);
        builder.set(DISCOVERY_INSTANCE_ID_KEY, serviceInstance.getInstanceId());
        return builder.build();
    }

    /**
     * Checks whether this instance should update its connections.
     *
     * @param newInstanceList The new instances that should be compared to the stored ones.
     * @return True, if the given instance list contains different entries than the stored ones.
     */
    protected boolean needsToUpdateConnections(final List<ServiceInstance> newInstanceList) {
        if (this.instanceList.size() != newInstanceList.size()) {
            return true;
        }
        for (final ServiceInstance instance : this.instanceList) {
            final int port = getGrpcPort(instance);
            boolean isSame = false;
            for (final ServiceInstance newInstance : newInstanceList) {
                final int newPort = getGrpcPort(newInstance);
                if (newInstance.getHost().equals(instance.getHost()) && port == newPort) {
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

    private void resolve() {
        log.debug("Scheduled resolve for {}", this.name);
        if (this.resolving) {
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
        this.instanceList = Lists.newArrayList();
        if (this.shutdownHook != null) {
            this.shutdownHook.accept(this);
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

        // The listener is stored in an extra variable to avoid NPEs if the resolver is shutdown while resolving
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
            final AtomicReference<List<ServiceInstance>> resultContainer = new AtomicReference<>(KEEP_PREVIOUS);
            try {
                resultContainer.set(resolveInternal());
            } catch (final Exception e) {
                this.savedListener.onError(Status.UNAVAILABLE.withCause(e)
                        .withDescription("Failed to update server list for " + getName()));
                resultContainer.set(Lists.newArrayList());
            } finally {
                DiscoveryClientNameResolver.this.syncContext.execute(() -> {
                    DiscoveryClientNameResolver.this.resolving = false;
                    final List<ServiceInstance> result = resultContainer.get();
                    if (result != KEEP_PREVIOUS && isActive()) {
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
            // Discover servers
            final List<ServiceInstance> newInstanceList = discoverServers();
            if (CollectionUtils.isEmpty(newInstanceList)) {
                log.error("No servers found for {}", getName());
                this.savedListener.onError(Status.UNAVAILABLE.withDescription("No servers found for " + getName()));
                return Lists.newArrayList();
            } else {
                log.debug("Got {} candidate servers for {}", newInstanceList.size(), getName());
            }

            // Check for changes
            if (!needsToUpdateConnections(newInstanceList)) {
                log.debug("Nothing has changed... skipping update for {}", getName());
                return KEEP_PREVIOUS;
            }

            // Set new servers
            log.debug("Ready to update server list for {}", getName());
            this.savedListener.onResult(ResolutionResult.newBuilder()
                    .setAddresses(toTargets(newInstanceList))
                    .setServiceConfig(resolveServiceConfig(newInstanceList))
                    .build());
            log.info("Done updating server list for {}", getName());
            return newInstanceList;
        }

        private List<EquivalentAddressGroup> toTargets(final List<ServiceInstance> newInstanceList) {
            final List<EquivalentAddressGroup> targets = Lists.newArrayList();
            for (final ServiceInstance instance : newInstanceList) {
                targets.add(toTarget(instance));
            }
            return targets;
        }

        private EquivalentAddressGroup toTarget(final ServiceInstance instance) {
            final String host = instance.getHost();
            final int port = getGrpcPort(instance);
            final Attributes attributes = getAttributes(instance);
            log.debug("Found gRPC server {}:{} for {}", host, port, getName());
            return new EquivalentAddressGroup(new InetSocketAddress(host, port), attributes);
        }

    }

}

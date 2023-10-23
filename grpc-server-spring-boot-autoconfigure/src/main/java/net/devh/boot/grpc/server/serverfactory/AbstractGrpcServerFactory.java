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

package net.devh.boot.grpc.server.serverfactory;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.unit.DataSize;

import com.google.common.collect.Lists;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;

/**
 * Abstract factory for grpc servers.
 *
 * @param <T> The type of builder used by this factory.
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @since 5/17/16
 */
@Slf4j
public abstract class AbstractGrpcServerFactory<T extends ServerBuilder<T>> implements GrpcServerFactory {

    private final List<GrpcServiceDefinition> serviceList = Lists.newLinkedList();

    protected final GrpcServerProperties properties;
    protected final List<GrpcServerConfigurer> serverConfigurers;

    /**
     * Creates a new server factory with the given properties.
     *
     * @param properties The properties used to configure the server.
     * @param serverConfigurers The server configurers to use. Can be empty.
     */
    protected AbstractGrpcServerFactory(final GrpcServerProperties properties,
            final List<GrpcServerConfigurer> serverConfigurers) {
        this.properties = requireNonNull(properties, "properties");
        this.serverConfigurers = requireNonNull(serverConfigurers, "serverConfigurers");
    }

    @Override
    public Server createServer() {
        final T builder = newServerBuilder();
        configure(builder);
        return builder.build();
    }

    /**
     * Creates a new server builder.
     *
     * @return The newly created server builder.
     */
    protected abstract T newServerBuilder();

    /**
     * Configures the given server builder. This method can be overwritten to add features that are not yet supported by
     * this library or use a {@link GrpcServerConfigurer} instead.
     *
     * @param builder The server builder to configure.
     */
    protected void configure(final T builder) {
        configureServices(builder);
        configureKeepAlive(builder);
        configureConnectionLimits(builder);
        configureSecurity(builder);
        configureLimits(builder);
        for (final GrpcServerConfigurer serverConfigurer : this.serverConfigurers) {
            serverConfigurer.accept(builder);
        }
    }

    /**
     * Configures the services that should be served by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureServices(final T builder) {
        final Set<String> serviceNames = new LinkedHashSet<>();

        for (final GrpcServiceDefinition service : this.serviceList) {
            final String serviceName = service.getDefinition().getServiceDescriptor().getName();
            if (!serviceNames.add(serviceName)) {
                throw new IllegalStateException("Found duplicate service implementation: " + serviceName);
            }
            log.info("Registered gRPC service: " + serviceName + ", bean: " + service.getBeanName() + ", class: "
                    + service.getBeanClazz().getName());
            builder.addService(service.getDefinition());
        }
    }

    /**
     * Configures the keep alive options that should be used by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureKeepAlive(final T builder) {
        if (this.properties.isEnableKeepAlive()) {
            throw new IllegalStateException("KeepAlive is enabled but this implementation does not support keepAlive!");
        }
    }

    /**
     * Configures the keep alive options that should be used by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureConnectionLimits(final T builder) {
        if (this.properties.getMaxConnectionIdle() != null) {
            throw new IllegalStateException(
                    "MaxConnectionIdle is set but this implementation does not support maxConnectionIdle!");
        }
        if (this.properties.getMaxConnectionAge() != null) {
            throw new IllegalStateException(
                    "MaxConnectionAge is set but this implementation does not support maxConnectionAge!");
        }
        if (this.properties.getMaxConnectionAgeGrace() != null) {
            throw new IllegalStateException(
                    "MaxConnectionAgeGrace is set but this implementation does not support maxConnectionAgeGrace!");
        }
    }

    /**
     * Configures the security options that should be used by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureSecurity(final T builder) {
        if (this.properties.getSecurity().isEnabled()) {
            throw new IllegalStateException("Security is enabled but this implementation does not support security!");
        }
    }

    /**
     * Configures limits such as max message sizes that should be used by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureLimits(final T builder) {
        final DataSize maxInboundMessageSize = this.properties.getMaxInboundMessageSize();
        if (maxInboundMessageSize != null) {
            builder.maxInboundMessageSize((int) maxInboundMessageSize.toBytes());
        }
        final DataSize maxInboundMetadataSize = this.properties.getMaxInboundMetadataSize();
        if (maxInboundMetadataSize != null) {
            builder.maxInboundMetadataSize((int) maxInboundMetadataSize.toBytes());
        }
    }

    @Override
    public String getAddress() {
        return this.properties.getAddress();
    }

    @Override
    public int getPort() {
        return this.properties.getPort();
    }

    @Override
    public void addService(final GrpcServiceDefinition service) {
        this.serviceList.add(service);
    }

}

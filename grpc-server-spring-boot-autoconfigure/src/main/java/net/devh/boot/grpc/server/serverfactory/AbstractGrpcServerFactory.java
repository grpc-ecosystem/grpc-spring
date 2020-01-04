/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.serverfactory;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.util.unit.DataSize;

import com.google.common.collect.Lists;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.services.HealthStatusManager;
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

    @Autowired
    private HealthStatusManager healthStatusManager;

    /**
     * Creates a new server factory with the given properties.
     *
     * @param properties The properties used to configure the server.
     * @param serverConfigurers The server configurers to use. Can be empty.
     */
    public AbstractGrpcServerFactory(final GrpcServerProperties properties,
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
        // support health check
        if (this.properties.isHealthServiceEnabled()) {
            builder.addService(this.healthStatusManager.getHealthService());
        }
        if (this.properties.isReflectionServiceEnabled()) {
            builder.addService(ProtoReflectionService.newInstance());
        }

        for (final GrpcServiceDefinition service : this.serviceList) {
            final String serviceName = service.getDefinition().getServiceDescriptor().getName();
            log.info("Registered gRPC service: " + serviceName + ", bean: " + service.getBeanName() + ", class: "
                    + service.getBeanClazz().getName());
            builder.addService(service.getDefinition());
            this.healthStatusManager.setStatus(serviceName, HealthCheckResponse.ServingStatus.SERVING);
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
     * Converts the given path to a file. This method checks that the file exists and refers to a file.
     *
     * @param context The context for what the file is used. This value will be used in case of exceptions.
     * @param path The path of the file to use.
     * @return The file instance created with the given path.
     * @deprecated Will be removed in a future version. Prefer spring's {@link Resource}s instead of plain files.
     */
    @Deprecated
    // TODO: Remove in 2.7.0
    protected File toCheckedFile(final String context, final String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException(context + " path cannot be null or blank");
        }
        final File file = new File(path);
        if (!file.isFile()) {
            String message =
                    context + " file does not exist or path does not refer to a file: '" + file.getPath() + "'";
            if (!file.isAbsolute()) {
                message += " (" + file.getAbsolutePath() + ")";
            }
            throw new IllegalArgumentException(message);
        }
        return file;
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

    @Override
    public void destroy() {
        for (final GrpcServiceDefinition grpcServiceDefinition : this.serviceList) {
            final String serviceName = grpcServiceDefinition.getDefinition().getServiceDescriptor().getName();
            this.healthStatusManager.clearStatus(serviceName);
        }
    }

}

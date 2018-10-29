/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.springboot.autoconfigure.grpc.server;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.Server;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.services.HealthStatusManager;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.GrpcServerProperties.Security;
import net.devh.springboot.autoconfigure.grpc.server.codec.CodecType;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

/**
 * Factory for netty based grpc servers.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Slf4j
public class NettyGrpcServerFactory implements GrpcServerFactory {

    private final GrpcServerProperties properties;

    private final List<GrpcServiceDefinition> serviceList = Lists.newLinkedList();

    private final List<GrpcCodecDefinition> codecList = Lists.newLinkedList();

    @Autowired
    private HealthStatusManager healthStatusManager;

    /**
     * Creates a new netty server factory with the given properties.
     *
     * @param properties The properties used to configure the server.
     */
    public NettyGrpcServerFactory(final GrpcServerProperties properties) {
        this.properties = requireNonNull(properties, "properties");
    }

    @Override
    public Server createServer() {
        final NettyServerBuilder builder =
                NettyServerBuilder.forAddress(new InetSocketAddress(InetAddresses.forString(getAddress()), getPort()));
        configure(builder);
        return builder.build();
    }

    /**
     * Configures the given netty server builder. This method can be overwritten to add features that are not yet
     * supported by this library.
     *
     * @param builder The server builder to configure.
     */
    protected void configure(final NettyServerBuilder builder) {
        configureServices(builder);
        configureSecurity(builder);
        configureLimits(builder);
        configureCodecs(builder);
    }

    /**
     * Configures the services that should be served by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureServices(final NettyServerBuilder builder) {
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
     * Configures the security options that should be used by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureSecurity(final NettyServerBuilder builder) {
        final Security security = this.properties.getSecurity();
        if (security.isEnabled()) {
            final File certificateChainFile = toCheckedFile("certificateChain", security.getCertificateChainPath());
            final File privateKeyFile = toCheckedFile("privateKey", security.getPrivateKeyPath());
            final SslContextBuilder sslContextBuilder = GrpcSslContexts.forServer(certificateChainFile, privateKeyFile);

            if (security.getClientAuth() != ClientAuth.NONE) {
                sslContextBuilder.clientAuth(security.getClientAuth());

                final String trustCertCollectionPath = security.getTrustCertCollectionPath();
                if (trustCertCollectionPath != null && !trustCertCollectionPath.isEmpty()) {
                    final File trustCertCollectionFile = toCheckedFile("trustCertCollection", trustCertCollectionPath);
                    sslContextBuilder.trustManager(trustCertCollectionFile);
                }
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (final SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc server", e);
            }
        }
    }

    /**
     * Converts the given path to a file. This method checks that the file exists and refers to a file.
     *
     * @param context The context for what the file is used. This value will be used in case of exceptions.
     * @param path The path of the file to use.
     * @return The file instance created with the given path.
     */
    private File toCheckedFile(final String context, final String path) {
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
    protected void configureLimits(final NettyServerBuilder builder) {
        final Integer maxInboundMessageSize = this.properties.getMaxInboundMessageSize();
        if (maxInboundMessageSize != null) {
            builder.maxInboundMessageSize(maxInboundMessageSize);
        }
    }

    /**
     * Configures the codecs that should be supported by the server.
     *
     * @param builder The server builder to configure.
     */
    protected void configureCodecs(final NettyServerBuilder builder) {
        if (this.codecList.isEmpty()) {
            final CompressorRegistry compressorRegistry = CompressorRegistry.newEmptyInstance();
            final DecompressorRegistry decompressorRegistry = DecompressorRegistry.emptyInstance();

            for (final GrpcCodecDefinition grpcCodecDefinition : this.codecList) {
                if (grpcCodecDefinition.getCodecType().equals(CodecType.COMPRESS)) {
                    compressorRegistry.register(grpcCodecDefinition.getCodec());
                } else if (grpcCodecDefinition.getCodecType().equals(CodecType.DECOMPRESS)) {
                    decompressorRegistry.with(grpcCodecDefinition.getCodec(), grpcCodecDefinition.isAdvertised());
                } else {
                    compressorRegistry.register(grpcCodecDefinition.getCodec());
                    decompressorRegistry.with(grpcCodecDefinition.getCodec(), grpcCodecDefinition.isAdvertised());
                }
            }

            builder.compressorRegistry(compressorRegistry);
            builder.decompressorRegistry(decompressorRegistry);
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
    public void addCodec(final GrpcCodecDefinition codec) {
        this.codecList.add(codec);
    }

    @Override
    public void destroy() {
        for (final GrpcServiceDefinition grpcServiceDefinition : this.serviceList) {
            final String serviceName = grpcServiceDefinition.getDefinition().getServiceDescriptor().getName();
            this.healthStatusManager.clearStatus(serviceName);
        }
    }

}

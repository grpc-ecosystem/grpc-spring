package net.devh.springboot.autoconfigure.grpc.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.Server;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.services.HealthStatusManager;
import lombok.extern.slf4j.Slf4j;
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

    public NettyGrpcServerFactory(final GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Server createServer() {
        final NettyServerBuilder builder = NettyServerBuilder.forAddress(
                new InetSocketAddress(InetAddresses.forString(getAddress()), getPort()));

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

        if (this.properties.getSecurity().isEnabled()) {
            final File certificateChain = new File(this.properties.getSecurity().getCertificateChainPath());
            final File certificate = new File(this.properties.getSecurity().getCertificatePath());
            builder.useTransportSecurity(certificateChain, certificate);
        }
        if (this.properties.getMaxMessageSize() > 0) {
            builder.maxInboundMessageSize(this.properties.getMaxMessageSize());
        }
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
        return builder.build();
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

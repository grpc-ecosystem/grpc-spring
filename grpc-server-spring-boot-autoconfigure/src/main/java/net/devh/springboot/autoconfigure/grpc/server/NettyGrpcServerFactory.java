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
import io.grpc.services.HealthStatusManager;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.codec.CodecType;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Slf4j
public class NettyGrpcServerFactory implements GrpcServerFactory {

    private final GrpcServerProperties properties;

    private final List<GrpcServiceDefinition> serviceList = Lists.newLinkedList();

    private final List<GrpcCodecDefinition> codecList = Lists.newLinkedList();

    @Autowired
    private HealthStatusManager healthStatusManager;

    public NettyGrpcServerFactory(GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Server createServer() {
        NettyServerBuilder builder = NettyServerBuilder.forAddress(
                new InetSocketAddress(InetAddresses.forString(getAddress()), getPort()));

        // support health check
        builder.addService(healthStatusManager.getHealthService());

        for (GrpcServiceDefinition service : this.serviceList) {
            String serviceName = service.getDefinition().getServiceDescriptor().getName();
            log.info("Registered gRPC service: " + serviceName + ", bean: " + service.getBeanName() + ", class: " + service.getBeanClazz().getName());
            builder.addService(service.getDefinition());
            healthStatusManager.setStatus(serviceName, HealthCheckResponse.ServingStatus.SERVING);
        }

        if (this.properties.getSecurity().getEnabled()) {
            File certificateChain = new File(this.properties.getSecurity().getCertificateChainPath());
            File certificate = new File(this.properties.getSecurity().getCertificatePath());
            builder.useTransportSecurity(certificateChain, certificate);
        }
        if(properties.getMaxMessageSize() > 0) {
        	builder.maxInboundMessageSize(properties.getMaxMessageSize());
        }
        if (codecList.size() > 0) {
            CompressorRegistry compressorRegistry = CompressorRegistry.newEmptyInstance();
            DecompressorRegistry decompressorRegistry = DecompressorRegistry.emptyInstance();

            for (GrpcCodecDefinition grpcCodecDefinition : this.codecList) {
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
    public void addService(GrpcServiceDefinition service) {
        this.serviceList.add(service);
    }

    @Override
    public void addCodec(GrpcCodecDefinition codec) {
        this.codecList.add(codec);
    }

    @Override
    public void destroy() {
        for (GrpcServiceDefinition grpcServiceDefinition : serviceList) {
            String serviceName = grpcServiceDefinition.getDefinition().getServiceDescriptor().getName();
            healthStatusManager.clearStatus(serviceName);
        }
    }
}

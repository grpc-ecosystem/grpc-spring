package net.devh.springboot.autoconfigure.grpc.server;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Slf4j
public class NettyGrpcServerFactory implements GrpcServerFactory {

    private final GrpcServerProperties properties;
    private final List<GrpcServiceDefinition> services = Lists.newLinkedList();

    public NettyGrpcServerFactory(GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Server createServer() {
        NettyServerBuilder builder = NettyServerBuilder.forAddress(
                new InetSocketAddress(InetAddresses.forString(getAddress()), getPort()));
        for (GrpcServiceDefinition service : this.services) {
            log.info("Registered gRPC service: " + service.getDefinition().getServiceDescriptor().getName() + ", bean: " + service.getBeanName() + ", class: " + service.getBeanClazz().getName());
            builder.addService(service.getDefinition());
        }

        if (this.properties.getSecurity().getEnabled()) {
            File certificateChain = new File(this.properties.getSecurity().getCertificateChainPath());
            File certificate = new File(this.properties.getSecurity().getCertificatePath());
            builder.useTransportSecurity(certificateChain, certificate);
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
        this.services.add(service);
    }

}

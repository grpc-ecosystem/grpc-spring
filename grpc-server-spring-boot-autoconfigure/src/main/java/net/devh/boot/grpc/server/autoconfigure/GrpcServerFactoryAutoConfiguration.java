/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.autoconfigure;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.condition.ConditionalOnInterprocessServer;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.NettyGrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.ShadedNettyGrpcServerFactory;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;
import net.devh.boot.grpc.server.service.GrpcServiceDiscoverer;

/**
 * The auto configuration that will create the {@link GrpcServerFactory}s and {@link GrpcServerLifecycle}s, if the
 * developer hasn't specified their own variant.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({GrpcServerFactory.class, GrpcServerLifecycle.class})
@AutoConfigureAfter(GrpcServerAutoConfiguration.class)
public class GrpcServerFactoryAutoConfiguration {

    // First try the shaded netty server
    /**
     * Creates a GrpcServerFactory using the shaded netty. This is the recommended default for gRPC.
     *
     * @param properties The properties used to configure the server.
     * @param serviceDiscoverer The discoverer used to identify the services that should be served.
     * @param serverConfigurers The server configurers that contain additional configuration for the server.
     * @return The shadedNettyGrpcServerFactory bean.
     */
    @ConditionalOnClass(name = {"io.grpc.netty.shaded.io.netty.channel.Channel",
            "io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder"})
    @Conditional(ConditionalOnInterprocessServer.class)
    @Bean
    public ShadedNettyGrpcServerFactory shadedNettyGrpcServerFactory(
            final GrpcServerProperties properties,
            final GrpcServiceDiscoverer serviceDiscoverer,
            final List<GrpcServerConfigurer> serverConfigurers) {

        log.info("Detected grpc-netty-shaded: Creating ShadedNettyGrpcServerFactory");
        final ShadedNettyGrpcServerFactory factory = new ShadedNettyGrpcServerFactory(properties, serverConfigurers);
        for (final GrpcServiceDefinition service : serviceDiscoverer.findGrpcServices()) {
            factory.addService(service);
        }
        return factory;
    }

    /**
     * The server lifecycle bean for a shaded netty based server.
     *
     * @param factory The factory used to create the lifecycle.
     * @param properties The server properties to use.
     * @return The inter-process server lifecycle bean.
     */
    @ConditionalOnBean(ShadedNettyGrpcServerFactory.class)
    @Bean
    public GrpcServerLifecycle shadedNettyGrpcServerLifecycle(
            final ShadedNettyGrpcServerFactory factory,
            final GrpcServerProperties properties) {

        return new GrpcServerLifecycle(factory, properties.getGracefullShutdownTimeout());
    }

    // Then try the normal netty server
    /**
     * Creates a GrpcServerFactory using the non-shaded netty. This is the fallback, if the shaded one is not present.
     *
     * @param properties The properties used to configure the server.
     * @param serviceDiscoverer The discoverer used to identify the services that should be served.
     * @param serverConfigurers The server configurers that contain additional configuration for the server.
     * @return The shadedNettyGrpcServerFactory bean.
     */
    @ConditionalOnMissingBean(ShadedNettyGrpcServerFactory.class)
    @Conditional(ConditionalOnInterprocessServer.class)
    @ConditionalOnClass(name = {"io.netty.channel.Channel", "io.grpc.netty.NettyServerBuilder"})
    @Bean
    public NettyGrpcServerFactory nettyGrpcServerFactory(
            final GrpcServerProperties properties,
            final GrpcServiceDiscoverer serviceDiscoverer,
            final List<GrpcServerConfigurer> serverConfigurers) {

        log.info("Detected grpc-netty: Creating NettyGrpcServerFactory");
        final NettyGrpcServerFactory factory = new NettyGrpcServerFactory(properties, serverConfigurers);
        for (final GrpcServiceDefinition service : serviceDiscoverer.findGrpcServices()) {
            factory.addService(service);
        }
        return factory;
    }

    /**
     * The server lifecycle bean for netty based server.
     *
     * @param factory The factory used to create the lifecycle.
     * @param properties The server properties to use.
     * @return The inter-process server lifecycle bean.
     */
    @ConditionalOnBean(NettyGrpcServerFactory.class)
    @Bean
    public GrpcServerLifecycle nettyGrpcServerLifecycle(
            final NettyGrpcServerFactory factory,
            final GrpcServerProperties properties) {

        return new GrpcServerLifecycle(factory, properties.getGracefullShutdownTimeout());
    }

    /**
     * Creates a GrpcServerFactory using the in-process-server, if a name is specified.
     *
     * @param properties The properties used to configure the server.
     * @param serviceDiscoverer The discoverer used to identify the services that should be served.
     * @return The shadedNettyGrpcServerFactory bean.
     */
    @ConditionalOnProperty(prefix = "grpc.server", name = "in-process-name")
    @Bean
    public InProcessGrpcServerFactory inProcessGrpcServerFactory(
            final GrpcServerProperties properties,
            final GrpcServiceDiscoverer serviceDiscoverer) {

        log.info("'grpc.server.in-process-name' is set: Creating InProcessGrpcServerFactory");
        final InProcessGrpcServerFactory factory = new InProcessGrpcServerFactory(properties);
        for (final GrpcServiceDefinition service : serviceDiscoverer.findGrpcServices()) {
            factory.addService(service);
        }
        return factory;
    }

    /**
     * The server lifecycle bean for the in-process-server.
     *
     * @param factory The factory used to create the lifecycle.
     * @param properties The server properties to use.
     * @return The in-process server lifecycle bean.
     */
    @ConditionalOnBean(InProcessGrpcServerFactory.class)
    @Bean
    public GrpcServerLifecycle inProcessGrpcServerLifecycle(
            final InProcessGrpcServerFactory factory,
            final GrpcServerProperties properties) {

        return new GrpcServerLifecycle(factory, properties.getGracefullShutdownTimeout());
    }

}

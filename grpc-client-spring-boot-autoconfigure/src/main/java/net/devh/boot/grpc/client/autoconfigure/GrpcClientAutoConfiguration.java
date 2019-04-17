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

package net.devh.boot.grpc.client.autoconfigure;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import net.devh.boot.grpc.client.channelfactory.InProcessChannelFactory;
import net.devh.boot.grpc.client.channelfactory.NettyChannelFactory;
import net.devh.boot.grpc.client.channelfactory.ShadedNettyChannelFactory;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.inject.GrpcClientBeanPostProcessor;
import net.devh.boot.grpc.client.interceptor.AnnotationGlobalClientInterceptorConfigurer;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.client.nameresolver.CompositeNameResolverFactory;
import net.devh.boot.grpc.client.nameresolver.ConfigMappedNameResolverFactory;
import net.devh.boot.grpc.client.nameresolver.StaticNameResolverProvider;
import net.devh.boot.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration;

/**
 * The auto configuration used by Spring-Boot that contains all beans to create and inject grpc clients into beans.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Configuration
@EnableConfigurationProperties
@AutoConfigureAfter(name = "org.springframework.cloud.client.CommonsClientAutoConfiguration",
        value = GrpcCommonCodecAutoConfiguration.class)
public class GrpcClientAutoConfiguration {

    @Bean
    public GrpcClientBeanPostProcessor grpcClientBeanPostProcessor(final ApplicationContext applicationContext) {
        return new GrpcClientBeanPostProcessor(applicationContext);
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcChannelsProperties grpcChannelsProperties() {
        return new GrpcChannelsProperties();
    }

    @Bean
    public GlobalClientInterceptorRegistry globalClientInterceptorRegistry() {
        return new GlobalClientInterceptorRegistry();
    }

    @Bean
    public AnnotationGlobalClientInterceptorConfigurer annotationGlobalClientInterceptorConfigurer() {
        return new AnnotationGlobalClientInterceptorConfigurer();
    }

    /**
     * Creates the load balancer configurer bean for the given load balancer factory.
     *
     * @param loadBalancerFactory The factory that should be used for all
     * @return The load balancer factory bean.
     *
     * @see ManagedChannelBuilder#loadBalancerFactory(io.grpc.LoadBalancer.Factory)
     *
     * @deprecated This method disables service-config-based policy selection, and may cause problems if NameResolver
     *             returns GRPCLB balancer addresses but a non-GRPCLB LoadBalancer is passed in here. Use
     *             {@link GrpcChannelProperties#setDefaultLoadBalancingPolicy(String)} instead.
     */
    @ConditionalOnSingleCandidate(LoadBalancer.Factory.class)
    @ConditionalOnMissingBean(name = "grpcLoadBalancerConfigurer")
    @Bean
    @SuppressWarnings("deprecation")
    @Deprecated
    public GrpcChannelConfigurer grpcLoadBalancerConfigurer(final LoadBalancer.Factory loadBalancerFactory) {
        return (channel, name) -> channel.loadBalancerFactory(loadBalancerFactory);
    }

    /**
     * Creates a new name resolver factory with the given channel properties. The properties are used to map the client
     * name to the actual service addresses. If you want to add more name resolver schemes or modify existing ones, you
     * can do that in the following ways:
     *
     * <ul>
     * <li>If you only rely on the client properties or other static beans, then you can simply add an entry to java's
     * service discovery for {@link io.grpc.NameResolverProvider}s.</li>
     * <li>If you need access to other beans, then you have to redefine this bean and use a
     * {@link CompositeNameResolverFactory} as the delegate for the {@link ConfigMappedNameResolverFactory}.</li>
     * </ul>
     *
     * @param channelProperties The properties for the channels.
     * @return The default config mapped name resolver factory.
     */
    @ConditionalOnMissingBean
    @Lazy // Not needed for InProcessChannelFactories
    @Bean
    public NameResolver.Factory grpcNameResolverFactory(final GrpcChannelsProperties channelProperties) {
        return new ConfigMappedNameResolverFactory(channelProperties, NameResolverProvider.asFactory(),
                StaticNameResolverProvider.STATIC_DEFAULT_URI_MAPPER);
    }

    @ConditionalOnBean(CompressorRegistry.class)
    @Bean
    public GrpcChannelConfigurer compressionChannelConfigurer(
            final CompressorRegistry registry) {
        return (builder, name) -> builder.compressorRegistry(registry);
    }

    @ConditionalOnBean(DecompressorRegistry.class)
    @Bean
    public GrpcChannelConfigurer decompressionChannelConfigurer(
            final DecompressorRegistry registry) {
        return (builder, name) -> builder.decompressorRegistry(registry);
    }

    @ConditionalOnMissingBean(GrpcChannelConfigurer.class)
    @Bean
    public List<GrpcChannelConfigurer> defaultChannelConfigurers() {
        return Collections.emptyList();
    }

    // First try the shaded netty channel factory
    @ConditionalOnMissingBean(GrpcChannelFactory.class)
    @ConditionalOnClass(name = {"io.grpc.netty.shaded.io.netty.channel.Channel",
            "io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder"})
    @Bean
    public GrpcChannelFactory shadedNettyGrpcChannelFactory(final GrpcChannelsProperties properties,
            final NameResolver.Factory nameResolverFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        return new ShadedNettyChannelFactory(properties, nameResolverFactory,
                globalClientInterceptorRegistry, channelConfigurers);
    }

    // Then try the normal netty channel factory
    @ConditionalOnMissingBean(GrpcChannelFactory.class)
    @ConditionalOnClass(name = {"io.netty.channel.Channel", "io.grpc.netty.NettyChannelBuilder"})
    @Bean
    public GrpcChannelFactory nettyGrpcChannelFactory(final GrpcChannelsProperties properties,
            final NameResolver.Factory nameResolverFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        return new NettyChannelFactory(properties, nameResolverFactory,
                globalClientInterceptorRegistry, channelConfigurers);
    }

    // Finally try the in process channel factory
    @ConditionalOnMissingBean(GrpcChannelFactory.class)
    @Bean
    public GrpcChannelFactory inProcessGrpcChannelFactory(final GrpcChannelsProperties properties,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        return new InProcessChannelFactory(properties, globalClientInterceptorRegistry, channelConfigurers);
    }

}

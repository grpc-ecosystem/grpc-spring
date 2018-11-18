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

package net.devh.springboot.autoconfigure.grpc.client;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.grpc.GrpcTracing;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.LoadBalancer;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import net.devh.springboot.autoconfigure.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration;
import net.devh.springboot.autoconfigure.grpc.common.autoconfigure.GrpcCommonTraceAutoConfiguration;

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

    @ConditionalOnMissingBean
    @Bean
    public LoadBalancer.Factory grpcLoadBalancerFactory() {
        return RoundRobinLoadBalancerFactory.getInstance();
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

    @ConditionalOnMissingBean(value = GrpcChannelFactory.class,
            type = "org.springframework.cloud.client.discovery.DiscoveryClient")
    @Bean
    public GrpcChannelFactory addressGrpcChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        return new AddressChannelFactory(properties, loadBalancerFactory, globalClientInterceptorRegistry,
                channelConfigurers);
    }

    @Configuration
    @ConditionalOnBean(DiscoveryClient.class)
    protected static class DiscoveryGrpcClientAutoConfiguration {

        @ConditionalOnMissingBean
        @Bean
        public GrpcChannelFactory discoveryClientChannelFactory(
                final GrpcChannelsProperties channels,
                final LoadBalancer.Factory loadBalancerFactory,
                final DiscoveryClient discoveryClient,
                final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
                final List<GrpcChannelConfigurer> channelConfigurers) {
            return new DiscoveryClientChannelFactory(channels, loadBalancerFactory, discoveryClient,
                    globalClientInterceptorRegistry, channelConfigurers);
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.sleuth.scheduled.enabled", matchIfMissing = true)
    @AutoConfigureAfter({TraceAutoConfiguration.class, GrpcCommonTraceAutoConfiguration.class})
    @ConditionalOnBean(GrpcTracing.class)
    protected static class TraceClientAutoConfiguration {

        @Bean
        public GlobalClientInterceptorConfigurer globalTraceClientInterceptorConfigurerAdapter(
                final GrpcTracing grpcTracing) {
            return registry -> registry.addClientInterceptors(grpcTracing.newClientInterceptor());
        }

    }

}

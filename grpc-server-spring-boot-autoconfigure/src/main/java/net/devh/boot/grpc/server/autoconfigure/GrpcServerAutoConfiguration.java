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

import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.Server;
import io.grpc.services.HealthStatusManager;
import net.devh.boot.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.interceptor.AnnotationGlobalServerInterceptorConfigurer;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import net.devh.boot.grpc.server.nameresolver.SelfNameResolverFactory;
import net.devh.boot.grpc.server.scope.GrpcRequestScope;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import net.devh.boot.grpc.server.service.AnnotationGrpcServiceDiscoverer;
import net.devh.boot.grpc.server.service.GrpcServiceDiscoverer;

/**
 * The auto configuration used by Spring-Boot that contains all beans to run a grpc server/service.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass(Server.class)
@AutoConfigureAfter(GrpcCommonCodecAutoConfiguration.class)
public class GrpcServerAutoConfiguration {

    /**
     * A scope that is valid for the duration of a grpc request.
     *
     * @return The grpc request scope bean.
     */
    @Bean
    public static GrpcRequestScope grpcRequestScope() {
        return new GrpcRequestScope();
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerProperties defaultGrpcServerProperties() {
        return new GrpcServerProperties();
    }

    /**
     * Lazily creates a {@link SelfNameResolverFactory} bean, that can be used by the client to connect to the server
     * itself.
     *
     * @param properties The properties to derive the address from.
     * @return The newly created {@link SelfNameResolverFactory} bean.
     */
    @ConditionalOnMissingBean
    @Bean
    @Lazy
    public SelfNameResolverFactory selfNameResolverFactory(final GrpcServerProperties properties) {
        return new SelfNameResolverFactory(properties);
    }

    @ConditionalOnMissingBean
    @Bean
    GlobalServerInterceptorRegistry globalServerInterceptorRegistry(
            final ApplicationContext applicationContext) {
        return new GlobalServerInterceptorRegistry(applicationContext);
    }

    @Bean
    @Lazy
    AnnotationGlobalServerInterceptorConfigurer annotationGlobalServerInterceptorConfigurer(
            final ApplicationContext applicationContext) {
        return new AnnotationGlobalServerInterceptorConfigurer(applicationContext);
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcServiceDiscoverer defaultGrpcServiceDiscoverer() {
        return new AnnotationGrpcServiceDiscoverer();
    }

    @ConditionalOnMissingBean
    @Bean
    public HealthStatusManager healthStatusManager() {
        return new HealthStatusManager();
    }

    @ConditionalOnBean(CompressorRegistry.class)
    @Bean
    public GrpcServerConfigurer compressionServerConfigurer(final CompressorRegistry registry) {
        return builder -> builder.compressorRegistry(registry);
    }

    @ConditionalOnBean(DecompressorRegistry.class)
    @Bean
    public GrpcServerConfigurer decompressionServerConfigurer(final DecompressorRegistry registry) {
        return builder -> builder.decompressorRegistry(registry);
    }

    @ConditionalOnMissingBean(GrpcServerConfigurer.class)
    @Bean
    public List<GrpcServerConfigurer> defaultServerConfigurers() {
        return Collections.emptyList();
    }

    @ConditionalOnMissingBean
    @ConditionalOnBean(GrpcServerFactory.class)
    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(final GrpcServerFactory factory, GrpcServerProperties properties) {
        return new GrpcServerLifecycle(factory, properties.getShutdownGracePeriod());
    }

}

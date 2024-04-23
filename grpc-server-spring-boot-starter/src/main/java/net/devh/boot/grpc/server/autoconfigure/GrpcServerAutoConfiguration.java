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

package net.devh.boot.grpc.server.autoconfigure;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.Server;
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
    public GrpcServerLifecycle grpcServerLifecycle(
            final GrpcServerFactory factory,
            final GrpcServerProperties properties,
            final ApplicationEventPublisher eventPublisher) {
        return new GrpcServerLifecycle(factory, properties.getShutdownGracePeriod(), eventPublisher);
    }

}

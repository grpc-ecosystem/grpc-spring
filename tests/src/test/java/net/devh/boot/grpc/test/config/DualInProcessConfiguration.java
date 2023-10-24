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

package net.devh.boot.grpc.test.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.inprocess.InProcessChannelBuilder;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import net.devh.boot.grpc.client.channelfactory.InProcessChannelFactory;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;
import net.devh.boot.grpc.server.service.GrpcServiceDiscoverer;

@Configuration
public class DualInProcessConfiguration {

    @Bean
    GrpcChannelFactory grpcChannelFactory(final GrpcChannelsProperties properties,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        return new InProcessChannelFactory(properties, globalClientInterceptorRegistry) {

            @Override
            protected InProcessChannelBuilder newChannelBuilder(final String name) {
                if (name.endsWith("-secondary")) {
                    return super.newChannelBuilder("test-secondary");
                }
                return super.newChannelBuilder("test"); // Use fixed inMemory channel name: test
            }

        };
    }

    @Bean
    GrpcServerFactory grpcServerFactory(final GrpcServerProperties properties,
            final GrpcServiceDiscoverer discoverer) {
        final InProcessGrpcServerFactory factory = new InProcessGrpcServerFactory("test", properties);
        for (final GrpcServiceDefinition service : discoverer.findGrpcServices()) {
            factory.addService(service);
        }
        return factory;
    }


    @Bean
    GrpcServerFactory grpcServerFactorySecondary(final GrpcServerProperties properties,
            final GrpcServiceDiscoverer discoverer) {
        final InProcessGrpcServerFactory factory = new InProcessGrpcServerFactory("test-secondary", properties);
        for (final GrpcServiceDefinition service : discoverer.findGrpcServices()) {
            factory.addService(service);
        }
        return factory;
    }

    @Bean
    GrpcServerLifecycle grpcServerLifecycle(
            @Qualifier("grpcServerFactory") final GrpcServerFactory grpcServerFactory,
            final GrpcServerProperties properties,
            final ApplicationEventPublisher eventPublisher) {
        return new GrpcServerLifecycle(grpcServerFactory, properties.getShutdownGracePeriod(), eventPublisher);
    }

    @Bean
    GrpcServerLifecycle grpcServerLifecycleSecondary(
            @Qualifier("grpcServerFactorySecondary") final GrpcServerFactory grpcServerFactory,
            final GrpcServerProperties properties,
            final ApplicationEventPublisher eventPublisher) {
        return new GrpcServerLifecycle(grpcServerFactory, properties.getShutdownGracePeriod(), eventPublisher);
    }

}

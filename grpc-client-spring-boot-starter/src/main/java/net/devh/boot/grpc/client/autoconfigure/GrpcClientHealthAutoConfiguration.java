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

package net.devh.boot.grpc.client.autoconfigure;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.google.common.collect.ImmutableMap;

import io.grpc.ConnectivityState;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;

/**
 * Auto configuration class for Spring-Boot. This allows zero config client health status updates for gRPC services.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(GrpcClientAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("grpcChannel")
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
public class GrpcClientHealthAutoConfiguration {

    /**
     * Creates a HealthIndicator based on the channels' {@link ConnectivityState}s from the underlying
     * {@link GrpcChannelFactory}.
     *
     * @param factory The factory to derive the connectivity states from.
     * @return A health indicator bean, that uses the following assumption
     *         <code>DOWN == states.contains(TRANSIENT_FAILURE)</code>.
     */
    @Bean
    @Lazy
    public HealthIndicator grpcChannelHealthIndicator(final GrpcChannelFactory factory) {
        return () -> {
            final ImmutableMap<String, ConnectivityState> states = ImmutableMap.copyOf(factory.getConnectivityState());
            final Health.Builder health;
            if (states.containsValue(ConnectivityState.TRANSIENT_FAILURE)) {
                health = Health.outOfService();
            } else {
                health = Health.up();
            }
            return health.withDetails(states)
                    .build();
        };
    }

}

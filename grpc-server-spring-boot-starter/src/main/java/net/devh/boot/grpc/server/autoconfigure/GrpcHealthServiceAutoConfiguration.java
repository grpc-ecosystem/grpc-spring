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

import net.devh.boot.grpc.server.health.ActuatorGrpcHealth;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.BindableService;
import io.grpc.protobuf.services.HealthStatusManager;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Auto configuration that sets up the grpc health service.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration
@ConditionalOnClass(HealthStatusManager.class)
@ConditionalOnProperty(prefix = "grpc.server", name = "health-service-enabled", matchIfMissing = true)
@AutoConfigureBefore(GrpcServerFactoryAutoConfiguration.class)
@AutoConfigureAfter(HealthEndpointAutoConfiguration.class)
public class GrpcHealthServiceAutoConfiguration {

    /**
     * Creates a new HealthStatusManager instance.
     *
     * @return The newly created bean.
     */
    @Bean
    @ConditionalOnMissingBean
    HealthStatusManager healthStatusManager() {
        return new HealthStatusManager();
    }

    @Bean
    @GrpcService
    @ConditionalOnProperty(prefix = "grpc.server", name = "health-service-type", havingValue = "GRPC", matchIfMissing = true)
    BindableService grpcHealthService(final HealthStatusManager healthStatusManager) {
        return healthStatusManager.getHealthService();
    }

    @Bean
    @GrpcService
    @ConditionalOnProperty(prefix = "grpc.server", name = "health-service-type", havingValue = "ACTUATOR")
    @ConditionalOnBean(HealthEndpoint.class)
    BindableService grpcHealthServiceActuator(final HealthEndpoint healthStatusManager) {
        return new ActuatorGrpcHealth(healthStatusManager);
    }
}

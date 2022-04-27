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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
    BindableService grpcHealthService(final HealthStatusManager healthStatusManager) {
        return healthStatusManager.getHealthService();
    }

}

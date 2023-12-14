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

import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.grpc.ClientInterceptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingClientInterceptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import net.devh.boot.grpc.common.util.InterceptorOrder;

/**
 * Auto configuration class for Spring-Boot. This allows zero config client metrics for gRPC services.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(CompositeMeterRegistryAutoConfiguration.class)
@AutoConfigureBefore(GrpcClientAutoConfiguration.class)
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnClass(MetricCollectingClientInterceptor.class)
public class GrpcClientMetricAutoConfiguration {

    /**
     * Creates a {@link ClientInterceptor} that collects metrics about incoming and outgoing requests and responses.
     *
     * @param registry The registry used to create the metrics.
     * @return The newly created MetricCollectingClientInterceptor bean.
     */
    @GrpcGlobalClientInterceptor
    @Order(InterceptorOrder.ORDER_TRACING_METRICS)
    @ConditionalOnMissingBean
    public MetricCollectingClientInterceptor metricCollectingClientInterceptor(final MeterRegistry registry) {
        return new MetricCollectingClientInterceptor(registry);
    }
}

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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.grpc.ClientInterceptor;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcClientInterceptor;
import io.micrometer.observation.ObservationRegistry;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import net.devh.boot.grpc.common.util.InterceptorOrder;

/**
 * The configuration used to configure micrometer tracing for grpc.
 *
 * @author Dave Syer (dsyer@vmware.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "management.tracing.grpc.enabled", matchIfMissing = true)
@AutoConfigureAfter(name = "org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration")
@ConditionalOnBean(ObservationRegistry.class)
public class GrpcClientMicrometerTraceAutoConfiguration {

    /**
     * Configures a global client interceptor that applies micrometer tracing logic to the requests.
     *
     * @param observations The observation registry bean.
     * @return The tracing client interceptor bean.
     */
    @GrpcGlobalClientInterceptor
    @Order(InterceptorOrder.ORDER_TRACING_METRICS + 1)
    ClientInterceptor globalObservationClientInterceptorConfigurer(final ObservationRegistry observations) {
        return new ObservationGrpcClientInterceptor(observations);
    }

}

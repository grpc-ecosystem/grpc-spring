/*
 * Copyright (c) 2016-2022 Michael Zhang <yidongnan@gmail.com>
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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.grpc.ClientInterceptor;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcClientInterceptor;
import io.micrometer.observation.ObservationRegistry;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import net.devh.boot.grpc.common.autoconfigure.GrpcCommonTraceAutoConfiguration;
import net.devh.boot.grpc.common.util.InterceptorOrder;

/**
 * The configuration used to configure micrometer tracing for grpc.
 *
 * @author Dave Syer (dsyer@vmware.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "management.tracing.grpc.enabled", matchIfMissing = true)
@AutoConfigureAfter(value = GrpcCommonTraceAutoConfiguration.class,
        name = "org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration")
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

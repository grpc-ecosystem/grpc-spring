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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcAdviceDiscoverer;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionHandler;
import net.devh.boot.grpc.server.advice.GrpcAdviceIsPresentCondition;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandlerMethodResolver;
import net.devh.boot.grpc.server.error.GrpcExceptionInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * The auto configuration that will create necessary beans to provide a proper exception handling via annotations
 * {@link GrpcAdvice @GrpcAdvice} and {@link GrpcExceptionHandler @GrpcExceptionHandler}.
 *
 * <p>
 * Exception handling via global server interceptors {@link GrpcGlobalServerInterceptor @GrpcGlobalServerInterceptor}.
 * </p>
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcAdvice
 * @see GrpcExceptionHandler
 * @see GrpcExceptionInterceptor
 */
@Configuration(proxyBeanMethods = false)
@Conditional(GrpcAdviceIsPresentCondition.class)
@AutoConfigureBefore(GrpcServerFactoryAutoConfiguration.class)
public class GrpcAdviceAutoConfiguration {

    @Bean
    public GrpcAdviceDiscoverer grpcAdviceDiscoverer() {
        return new GrpcAdviceDiscoverer();
    }

    @Bean
    public GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver(
            final GrpcAdviceDiscoverer grpcAdviceDiscoverer) {
        return new GrpcExceptionHandlerMethodResolver(grpcAdviceDiscoverer);
    }

    @Bean
    public GrpcAdviceExceptionHandler grpcAdviceExceptionHandler(
            GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver) {
        return new GrpcAdviceExceptionHandler(grpcExceptionHandlerMethodResolver);
    }

    @GrpcGlobalServerInterceptor
    @Order(InterceptorOrder.ORDER_GLOBAL_EXCEPTION_HANDLING)
    public GrpcExceptionInterceptor grpcAdviceExceptionInterceptor(
            GrpcAdviceExceptionHandler grpcAdviceExceptionHandler) {
        return new GrpcExceptionInterceptor(grpcAdviceExceptionHandler);
    }

}

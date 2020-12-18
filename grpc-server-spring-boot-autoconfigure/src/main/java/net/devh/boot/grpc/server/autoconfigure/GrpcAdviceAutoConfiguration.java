/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcAdviceDiscoverer;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionHandler;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionInterceptor;
import net.devh.boot.grpc.server.advice.GrpcAdviceIsPresent;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandlerMethodResolver;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * The auto configuration that will create necessary beans to provide a proper exception handling via annotations
 * {@link GrpcAdvice @GrpcAdvice} and {@link GrpcExceptionHandler @GrpcExceptionHandler}.
 * <p>
 * Exception handling via global server interceptors {@link GrpcGlobalServerInterceptor @GrpcGlobalServerInterceptor}.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcAdvice
 * @see GrpcExceptionHandler
 * @see GrpcAdviceExceptionInterceptor
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@Conditional(GrpcAdviceIsPresent.class)
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
    public GrpcAdviceExceptionInterceptor grpcAdviceExceptionInterceptor(
            GrpcAdviceExceptionHandler grpcAdviceExceptionHandler) {
        return new GrpcAdviceExceptionInterceptor(grpcAdviceExceptionHandler);
    }

}

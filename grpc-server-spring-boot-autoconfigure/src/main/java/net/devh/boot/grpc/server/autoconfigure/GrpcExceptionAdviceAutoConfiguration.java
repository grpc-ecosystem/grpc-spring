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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import net.devh.boot.grpc.server.service.exceptionhandling.GrpcExceptionHandler;
import net.devh.boot.grpc.server.service.exceptionhandling.GrpcExceptionHandlerMethodResolver;
import net.devh.boot.grpc.server.service.exceptionhandling.GrpcServiceAdvice;
import net.devh.boot.grpc.server.service.exceptionhandling.GrpcServiceAdviceDiscoverer;
import net.devh.boot.grpc.server.service.exceptionhandling.GrpcServiceAdviceExceptionHandler;
import net.devh.boot.grpc.server.service.exceptionhandling.GrpcServiceAdviceIsPresent;

/**
 * The auto configuration that will create necessary beans to provide a proper exception handling via annotations
 * {@link GrpcServiceAdvice @GrpcServiceAdvice} and {@link GrpcExceptionHandler @GrpcExceptionHandler}.
 * <p>
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcServiceAdvice
 * @see GrpcExceptionHandler
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@Conditional(GrpcServiceAdviceIsPresent.class)
@AutoConfigureAfter(GrpcServerAutoConfiguration.class)
public class GrpcExceptionAdviceAutoConfiguration {

    @Bean
    public GrpcServiceAdviceDiscoverer grpcServiceAdviceDiscoverer() {
        return new GrpcServiceAdviceDiscoverer();
    }

    @Bean
    public GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver(
            final GrpcServiceAdviceDiscoverer grpcServiceAdviceDiscoverer) {
        return new GrpcExceptionHandlerMethodResolver(grpcServiceAdviceDiscoverer);
    }

    @Bean
    public GrpcServiceAdviceExceptionHandler grpcServiceAdviceExceptionHandler(
            GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver) {
        return new GrpcServiceAdviceExceptionHandler(grpcExceptionHandlerMethodResolver);
    }

}

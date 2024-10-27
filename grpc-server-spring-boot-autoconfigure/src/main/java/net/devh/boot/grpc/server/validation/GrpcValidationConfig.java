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

package net.devh.boot.grpc.server.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * In Order to have valid requests this autoconfiguration is looking for marker annotation
 * {@link GrpcConstraint @GrpcConstraint}. In case of success, all necessary beans are being instantiated.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcConstraint
 * @see GrpcValidationResolver
 * @see RequestValidationInterceptor
 */
@Configuration
@Conditional(GrpcConstraintIsPresent.class)
class GrpcValidationConfig {

    @Bean
    GrpcValidationResolver grpcValidationResolver() {
        return new GrpcValidationResolver();
    }

    @GrpcGlobalServerInterceptor
    @Order(InterceptorOrder.ORDER_SERVER_REQUEST_VALIDATION)
    RequestValidationInterceptor requestValidationInterceptor(final GrpcValidationResolver grpcValidationResolver) {
        return new RequestValidationInterceptor(grpcValidationResolver);
    }

}

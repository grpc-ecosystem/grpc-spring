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

package net.devh.boot.grpc.server.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;

/**
 * Annotation that marks gRPC services that should be registered with a gRPC server. If spring-boot's auto configuration
 * is used, then the server will be created automatically. This annotation should only be added to implementations of
 * {@link BindableService} (GrpcService-ImplBase).
 *
 * <p>
 * <b>Note:</b> These annotation allows the specification of custom interceptors. These will be appended to the global
 * interceptors and applied using {@link ServerInterceptors#interceptForward(BindableService, ServerInterceptor...)}.
 * </p>
 *
 * @author Michael (yidongnan@gmail.com)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
@Bean
public @interface GrpcService {

    /**
     * A list of {@link ServerInterceptor} classes that should be applied to only this service. If a bean of the given
     * type exists, it will be used; otherwise a new instance of that class will be created via no-args constructor.
     *
     * <p>
     * <b>Note:</b> Please read the javadocs regarding the ordering of interceptors.
     * </p>
     *
     * @return A list of ServerInterceptor classes that should be used.
     */
    Class<? extends ServerInterceptor>[] interceptors() default {};

    /**
     * A list of {@link ServerInterceptor} beans that should be applied to only this service.
     *
     * <p>
     * <b>Note:</b> Please read the javadocs regarding the ordering of interceptors.
     * </p>
     *
     * @return A list of ServerInterceptor beans that should be used.
     */
    String[] interceptorNames() default {};

    /**
     * Whether the custom interceptors should be mixed with the global interceptors and sorted afterwards. Use this
     * option if you want to add a custom interceptor between global interceptors.
     *
     * @return True, if the custom interceptors should be merged with the global ones and sorted afterwards. False
     *         otherwise.
     */
    boolean sortInterceptors() default false;

}

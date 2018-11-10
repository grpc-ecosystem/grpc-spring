/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.springboot.autoconfigure.grpc.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;

/**
 * Annotation that marks gRPC services that should be registered with a gRPC server. If spring-boot's auto configuration
 * is used, then the server will be created automatically. This annotation should only be added to implementations of
 * {@link BindableService} (GrpcService-ImplBase).
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface GrpcService {

    // Unused - Should be removed
    @Deprecated
    Class<?> value() default void.class;

    /**
     * A list of {@link ServerInterceptor} classes that should be applied to only this service. If a bean of the given
     * type exists, it will be used; otherwise a new instance of that class will be created via no-args constructor.
     *
     * <p>
     * <b>Note:</b> These interceptors will be applied after the global interceptors. But the interceptors that were
     * applied last, will be called first.
     * </p>
     *
     * @return A list of ServerInterceptor classes that should be used.
     */
    Class<? extends ServerInterceptor>[] interceptors() default {};

    /**
     * A list of {@link ServerInterceptor} beans that should be applied to only this service.
     *
     * <p>
     * <b>Note:</b> These interceptors will be applied after the global interceptors and the interceptor classes. But
     * the interceptors that were applied last, will be called first.
     * </p>
     *
     * @return A list of ServerInterceptor beans that should be used.
     */
    String[] interceptorNames() default {};

}

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

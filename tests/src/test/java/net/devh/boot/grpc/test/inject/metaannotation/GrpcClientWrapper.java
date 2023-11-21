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

package net.devh.boot.grpc.test.inject.metaannotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import io.grpc.ClientInterceptor;
import net.devh.boot.grpc.client.inject.GrpcClient;

@GrpcClient(value = "")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface GrpcClientWrapper {

    @AliasFor(annotation = GrpcClient.class, attribute = "value")
    String value();

    @AliasFor(annotation = GrpcClient.class, attribute = "interceptors")
    Class<? extends ClientInterceptor>[] interceptors() default {};

    @AliasFor(annotation = GrpcClient.class, attribute = "interceptorNames")
    String[] interceptorNames() default {};

    @AliasFor(annotation = GrpcClient.class, attribute = "sortInterceptors")
    boolean sortInterceptors() default false;

    String extraParamString();

    boolean extraParamBoolean() default false;

}

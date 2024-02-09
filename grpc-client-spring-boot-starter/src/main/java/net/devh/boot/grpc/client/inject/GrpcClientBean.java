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

package net.devh.boot.grpc.client.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Annotation that can be added to {@link Configuration} classes to add a {@link GrpcClient} bean to the
 * {@link ApplicationContext}.
 */
@Target(ElementType.TYPE)
@Repeatable(GrpcClientBeans.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcClientBean {

    /**
     * The type of the bean to create.
     *
     * @return The type of the bean.
     */
    Class<?> clazz();

    /**
     * The name of the bean to create. If empty, a name will be generated automatically based on the bean class and the
     * client name.
     *
     * @return The optional name of the bean.
     */
    String beanName() default "";

    /**
     * The client definition used to create the channel and grab all properties.
     *
     * @return The client definition to use.
     */
    GrpcClient client();

}

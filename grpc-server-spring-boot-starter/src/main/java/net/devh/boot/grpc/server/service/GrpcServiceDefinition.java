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

import io.grpc.ServerServiceDefinition;

/**
 * Container class that contains all relevant information about a grpc service.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 * @see GrpcServiceDiscoverer
 */
public class GrpcServiceDefinition {

    private final String beanName;
    private final Class<?> beanClazz;
    private final ServerServiceDefinition definition;

    /**
     * Creates a new GrpcServiceDefinition.
     *
     * @param beanName The name of the grpc service bean in the spring context.
     * @param beanClazz The class of the grpc service bean.
     * @param definition The grpc service definition.
     */
    public GrpcServiceDefinition(final String beanName, final Class<?> beanClazz,
            final ServerServiceDefinition definition) {
        this.beanName = beanName;
        this.beanClazz = beanClazz;
        this.definition = definition;
    }

    /**
     * Gets the name of the grpc service bean.
     *
     * @return The name of the bean.
     */
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * Gets the class of the grpc service bean.
     *
     * @return The class of the grpc service bean.
     */
    public Class<?> getBeanClazz() {
        return this.beanClazz;
    }

    /**
     * Gets the grpc service definition.
     *
     * @return The grpc service definition.
     */
    public ServerServiceDefinition getDefinition() {
        return this.definition;
    }

}

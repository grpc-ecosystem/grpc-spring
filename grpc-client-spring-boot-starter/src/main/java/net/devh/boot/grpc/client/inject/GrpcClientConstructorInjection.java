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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;

class GrpcClientConstructorInjection {

    public static final String BEAN_NAME = "grpcClientConstructorInjection";

    private final List<Registry> injections = new ArrayList<>();

    static class Registry {

        private final Class<?> stubClazz;
        private final GrpcClient client;
        private final Class<?> targetClazz;
        private final BeanDefinition targetBeanDefinition;
        private final int constructorArgumentIndex;

        public Registry(final Class<?> stubClazz, final GrpcClient client, final Class<?> targetClazz,
                final BeanDefinition targetBeanDefinition, final int constructorArgumentIndex) {
            this.stubClazz = stubClazz;
            this.client = client;
            this.targetClazz = targetClazz;
            this.targetBeanDefinition = targetBeanDefinition;
            this.constructorArgumentIndex = constructorArgumentIndex;
        }

        public Class<?> getStubClass() {
            return this.stubClazz;
        }

        public GrpcClient getClient() {
            return this.client;
        }

        public Class<?> getTargetClazz() {
            return this.targetClazz;
        }

        public BeanDefinition getTargetBeanDefinition() {
            return this.targetBeanDefinition;
        }

        public int getConstructorArgumentIndex() {
            return this.constructorArgumentIndex;
        }
    }

    public List<Registry> getRegistries() {
        return this.injections;
    }

    public GrpcClientConstructorInjection add(final Registry injection) {
        this.injections.add(injection);
        return this;
    }

    public boolean isEmpty() {
        return this.injections.isEmpty();
    }

}

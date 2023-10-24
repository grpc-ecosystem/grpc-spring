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

package net.devh.boot.grpc.examples.cloud.client;

import org.springframework.context.annotation.Configuration;

import io.grpc.ClientInterceptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

/**
 * Example configuration class that adds a {@link ClientInterceptor} to the global interceptor list.
 */
@Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {

    /**
     * Creates a new {@link LogGrpcInterceptor} bean and adds it to the global interceptor list. As an alternative you
     * can directly annotate the {@code LogGrpcInterceptor} class and it will automatically be picked up by spring's
     * classpath scanning.
     *
     * @return The newly created bean.
     */
    @GrpcGlobalClientInterceptor
    LogGrpcInterceptor logClientInterceptor() {
        return new LogGrpcInterceptor();
    }

}

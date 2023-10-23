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

package net.devh.boot.grpc.client.interceptor;

import java.util.List;

import io.grpc.ClientInterceptor;

/**
 * A bean that can be used to configure global {@link ClientInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@FunctionalInterface
public interface GlobalClientInterceptorConfigurer {

    /**
     * Configures the given list of client interceptors, possibly adding new elements, removing unwanted elements, or
     * reordering the existing ones.
     *
     * @param interceptors A mutable list of client interceptors to configure.
     */
    void configureClientInterceptors(List<ClientInterceptor> interceptors);

}

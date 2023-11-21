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

import static com.google.common.collect.Maps.transformValues;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;

import io.grpc.ClientInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automatically find and configure {@link GrpcGlobalClientInterceptor annotated} global {@link ClientInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class AnnotationGlobalClientInterceptorConfigurer implements GlobalClientInterceptorConfigurer {

    private final ApplicationContext applicationContext;

    /**
     * Creates a new AnnotationGlobalClientInterceptorConfigurer.
     *
     * @param applicationContext The application context to fetch the {@link GrpcGlobalClientInterceptor} annotated
     *        {@link ClientInterceptor} beans from.
     */
    public AnnotationGlobalClientInterceptorConfigurer(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    /**
     * Helper method used to get the {@link GrpcGlobalClientInterceptor} annotated {@link ClientInterceptor}s from the
     * application context.
     *
     * @return A map containing the global interceptor beans.
     */
    protected Map<String, ClientInterceptor> getClientInterceptorBeans() {
        return transformValues(this.applicationContext.getBeansWithAnnotation(GrpcGlobalClientInterceptor.class),
                ClientInterceptor.class::cast);
    }

    @Override
    public void configureClientInterceptors(final List<ClientInterceptor> interceptors) {
        for (final Entry<String, ClientInterceptor> entry : getClientInterceptorBeans().entrySet()) {
            final ClientInterceptor interceptor = entry.getValue();
            log.debug("Registering GlobalClientInterceptor: {} ({})", entry.getKey(), interceptor);
            interceptors.add(interceptor);
        }
    }

}

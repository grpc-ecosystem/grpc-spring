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

package net.devh.boot.grpc.server.interceptor;

import static com.google.common.collect.Maps.transformValues;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;

import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automatically find and configure {@link GrpcGlobalServerInterceptor annotated} global {@link ServerInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class AnnotationGlobalServerInterceptorConfigurer implements GlobalServerInterceptorConfigurer {

    private final ApplicationContext applicationContext;

    /**
     * Creates a new AnnotationGlobalServerInterceptorConfigurer.
     *
     * @param applicationContext The application context to fetch the {@link GrpcGlobalServerInterceptor} annotated
     *        {@link ServerInterceptor} beans from.
     */
    public AnnotationGlobalServerInterceptorConfigurer(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    /**
     * Helper method used to get the {@link GrpcGlobalServerInterceptor} annotated {@link ServerInterceptor}s from the
     * application context.
     *
     * @return A map containing the global interceptor beans.
     */
    protected Map<String, ServerInterceptor> getServerInterceptorBeans() {
        return transformValues(this.applicationContext.getBeansWithAnnotation(GrpcGlobalServerInterceptor.class),
                ServerInterceptor.class::cast);
    }

    @Override
    public void configureServerInterceptors(final List<ServerInterceptor> interceptors) {
        for (final Entry<String, ServerInterceptor> entry : getServerInterceptorBeans().entrySet()) {
            final ServerInterceptor interceptor = entry.getValue();
            log.debug("Registering GlobalServerInterceptor: {} ({})", entry.getKey(), interceptor);
            interceptors.add(interceptor);
        }
    }

}

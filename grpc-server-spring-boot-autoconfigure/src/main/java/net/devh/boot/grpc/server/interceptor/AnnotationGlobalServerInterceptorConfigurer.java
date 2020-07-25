/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

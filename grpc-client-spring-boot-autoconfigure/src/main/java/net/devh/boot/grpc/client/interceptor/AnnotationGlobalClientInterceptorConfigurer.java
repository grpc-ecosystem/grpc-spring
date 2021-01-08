/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import io.grpc.ClientInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automatically find and configure {@link GrpcGlobalClientInterceptor annotated} global {@link ClientInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class AnnotationGlobalClientInterceptorConfigurer implements GlobalClientInterceptorConfigurer {

    @Autowired
    private ApplicationContext context;

    @Override
    public void addClientInterceptors(final GlobalClientInterceptorRegistry registry) {

        Map<String, Object> interceptorBeans = this.context.getBeansWithAnnotation(GrpcGlobalClientInterceptor.class)
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(AnnotationAwareOrderComparator.INSTANCE))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        interceptorBeans.forEach((name, interceptor) -> {
            log.debug("Registering GlobalClientInterceptor: {} ({})", name, interceptor);
            registry.addClientInterceptors((ClientInterceptor) interceptor);
        });
    }

}

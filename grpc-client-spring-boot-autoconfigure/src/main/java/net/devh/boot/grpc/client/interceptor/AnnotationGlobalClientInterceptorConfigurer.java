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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
        final String[] names = this.context.getBeanNamesForAnnotation(GrpcGlobalClientInterceptor.class);
        final List<String> sortedInterceptorNames = sortBasedOnOrder(names);
        for (final String name : sortedInterceptorNames) {
            final ClientInterceptor interceptor = this.context.getBean(name, ClientInterceptor.class);
            log.debug("Registering GlobalClientInterceptor: {} ({})", name, interceptor);
            registry.addClientInterceptors(interceptor);
        }
    }

    private List<String> sortBasedOnOrder(final String[] interceptorNames) {
        Map<Integer, String> orderedInterceptorNames = Maps.newTreeMap();
        List<String> unorderedInterceptorNames = Lists.newArrayList();
        for (final String name : interceptorNames) {
            Order interceptorOrder = this.context.findAnnotationOnBean(name, Order.class);
            if (interceptorOrder != null) {
                orderedInterceptorNames.put(Integer.valueOf(interceptorOrder.value()), name);
            } else {
                unorderedInterceptorNames.add(name);
            }
        }
        List<String> sortedInterceptorNames = Lists.newArrayList();
        sortedInterceptorNames.addAll(unorderedInterceptorNames);
        sortedInterceptorNames.addAll(orderedInterceptorNames.values());
        return sortedInterceptorNames;
    }

}

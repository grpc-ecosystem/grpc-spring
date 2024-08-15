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

package net.devh.boot.grpc.common.util;

import java.util.Comparator;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;

import com.google.common.collect.HashBiMap;

/**
 * A utility class with constants that can be used to configure the order of interceptors.
 *
 * <p>
 * <b>Note:</b> The order constants provided by this class are just a suggestion to simplify the interoperability of
 * multiple libraries and may be overridden. This library will use them for their own interceptors though.
 * </p>
 */
public final class InterceptorOrder {

    /**
     * The order value for interceptors that should be executed first. This is equivalent to
     * {@link Ordered#HIGHEST_PRECEDENCE}.
     */
    public static final int ORDER_FIRST = Ordered.HIGHEST_PRECEDENCE;
    /**
     * The order value for global exception handling interceptors.
     */
    public static final int ORDER_GLOBAL_EXCEPTION_HANDLING = 3000;
    /**
     * The order value for tracing and metrics collecting interceptors.
     */
    public static final int ORDER_TRACING_METRICS = 2500;
    /**
     * The order value for interceptors related security exception handling.
     */
    public static final int ORDER_SECURITY_EXCEPTION_HANDLING = 5000;
    /**
     * The order value for security interceptors related to authentication.
     */
    public static final int ORDER_SECURITY_AUTHENTICATION = 5100;
    /**
     * The order value for security interceptors related to authorization checks.
     */
    public static final int ORDER_SECURITY_AUTHORISATION = 5200;
    /**
     * The order value for interceptors that should be executed last. This is equivalent to
     * {@link Ordered#LOWEST_PRECEDENCE}. This is the default for interceptors without specified priority.
     */
    public static final int ORDER_LAST = Ordered.LOWEST_PRECEDENCE;

    private InterceptorOrder() {}

    /**
     * Creates a new Comparator that takes {@link Order} annotations on bean factory methods into account.
     *
     * @param context The application context to get the bean factory annotations form.
     * @param beanType The type of the bean you wish to sort.
     * @return A newly created comparator for beans.
     */
    public static Comparator<Object> beanFactoryAwareOrderComparator(final ApplicationContext context,
            final Class<?> beanType) {
        final Map<?, String> beans = HashBiMap.create(context.getBeansOfType(beanType)).inverse();
        return OrderComparator.INSTANCE.withSourceProvider(bean -> {

            // The AnnotationAwareOrderComparator does not have the "withSourceProvider" method
            // The OrderComparator.withSourceProvider does not properly account for the annotations
            final Integer priority = AnnotationAwareOrderComparator.INSTANCE.getPriority(bean);
            if (priority != null) {
                return (Ordered) () -> priority;
            }

            // Consult the bean factory method for annotations
            final String beanName = beans.get(bean);
            if (beanName != null) {
                final Order order = context.findAnnotationOnBean(beanName, Order.class);
                if (order != null) {
                    return (Ordered) order::value;
                }
            }

            // Nothing present
            return null;
        });
    }

}

/*
 * Copyright (c) 2016-2022 Michael Zhang <yidongnan@gmail.com>
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
    public static final int ORDER_GLOBAL_EXCEPTION_HANDLING = 0;
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

    private InterceptorOrder() {}

}

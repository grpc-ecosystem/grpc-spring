/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.common.metric;

import static net.devh.boot.grpc.common.metric.MetricConstants.TAG_METHOD_NAME;
import static net.devh.boot.grpc.common.metric.MetricConstants.TAG_SERVICE_NAME;

import io.grpc.MethodDescriptor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;

/**
 * Utility class that contains methods to create {@link Meter} instances for {@link MethodDescriptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class MetricUtils {

    /**
     * Creates a new counter builder for the given method. By default the base unit will be messages.
     *
     * @param method The method the counter will be created for.
     * @param name The name of the counter to use.
     * @param decription The description of the counter to use.
     * @return The newly created counter builder.
     */
    public static Counter.Builder prepareCounterFor(final MethodDescriptor<?, ?> method,
            final String name, final String decription) {
        return Counter.builder(name)
                .description(decription)
                .baseUnit("messages")
                .tag(TAG_SERVICE_NAME, extractServiceName(method))
                .tag(TAG_METHOD_NAME, extractMethodName(method));
    }

    /**
     * Creates a new timer builder for the given method.
     *
     * @param method The method the timer will be created for.
     * @param name The name of the timer to use.
     * @param decription The description of the timer to use.
     * @return The newly created timer builder.
     */
    public static Timer.Builder prepareTimerFor(final MethodDescriptor<?, ?> method,
            final String name, final String decription) {
        return Timer.builder(name)
                .description(decription)
                .tag(TAG_SERVICE_NAME, extractServiceName(method))
                .tag(TAG_METHOD_NAME, extractMethodName(method));
    }

    /**
     * Extracts the service name from the given method.
     *
     * @param method The method to get the service name from.
     * @return The extracted service name.
     * @see MethodDescriptor#extractFullServiceName(String)
     * @see #extractMethodName(MethodDescriptor)
     */
    public static String extractServiceName(final MethodDescriptor<?, ?> method) {
        return MethodDescriptor.extractFullServiceName(method.getFullMethodName());
    }

    /**
     * Extracts the method name from the given method.
     *
     * @param method The method to get the method name from.
     * @return The extracted method name.
     * @see #extractServiceName(MethodDescriptor)
     */
    public static String extractMethodName(final MethodDescriptor<?, ?> method) {
        // This method is the equivalent of MethodDescriptor.extractFullServiceName
        final String fullMethodName = method.getFullMethodName();
        final int index = fullMethodName.lastIndexOf('/');
        if (index == -1) {
            return fullMethodName;
        }
        return fullMethodName.substring(index + 1);
    }

    private MetricUtils() {}

}

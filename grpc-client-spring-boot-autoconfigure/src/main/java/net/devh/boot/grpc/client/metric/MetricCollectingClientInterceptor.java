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

package net.devh.boot.grpc.client.metric;

import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_PROCESSING_DURATION;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_REQUESTS_SENT;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_RESPONSES_RECEIVED;
import static net.devh.boot.grpc.common.metric.MetricUtils.prepareCounterFor;
import static net.devh.boot.grpc.common.metric.MetricUtils.prepareTimerFor;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import io.grpc.Status.Code;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import net.devh.boot.grpc.common.metric.AbstractMetricCollectingInterceptor;
import net.devh.boot.grpc.common.util.InterceptorOrder;

/**
 * A gRPC client interceptor that will collect metrics for micrometer.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@GrpcGlobalClientInterceptor
@Order(InterceptorOrder.ORDER_TRACING_METRICS)
public class MetricCollectingClientInterceptor extends AbstractMetricCollectingInterceptor
        implements ClientInterceptor {

    /**
     * Creates a new gRPC client interceptor that will collect metrics into the given {@link MeterRegistry}.
     *
     * @param registry The registry to use.
     */
    @Autowired
    public MetricCollectingClientInterceptor(final MeterRegistry registry) {
        super(registry);
    }

    /**
     * Creates a new gRPC client interceptor that will collect metrics into the given {@link MeterRegistry} and uses the
     * given customizer to configure the {@link Counter}s and {@link Timer}s.
     *
     * @param registry The registry to use.
     * @param counterCustomizer The unary function that can be used to customize the created counters.
     * @param timerCustomizer The unary function that can be used to customize the created timers.
     * @param eagerInitializedCodes The status codes that should be eager initialized.
     */
    public MetricCollectingClientInterceptor(final MeterRegistry registry,
            final UnaryOperator<Counter.Builder> counterCustomizer,
            final UnaryOperator<Timer.Builder> timerCustomizer, final Code... eagerInitializedCodes) {
        super(registry, counterCustomizer, timerCustomizer, eagerInitializedCodes);
    }

    @Override
    protected Counter newRequestCounterFor(final MethodDescriptor<?, ?> method) {
        return this.counterCustomizer.apply(
                prepareCounterFor(method,
                        METRIC_NAME_CLIENT_REQUESTS_SENT,
                        "The total number of requests sent"))
                .register(this.registry);
    }

    @Override
    protected Counter newResponseCounterFor(final MethodDescriptor<?, ?> method) {
        return this.counterCustomizer.apply(
                prepareCounterFor(method,
                        METRIC_NAME_CLIENT_RESPONSES_RECEIVED,
                        "The total number of responses received"))
                .register(this.registry);
    }

    @Override
    protected Function<Code, Timer> newTimerFunction(final MethodDescriptor<?, ?> method) {
        return asTimerFunction(() -> this.timerCustomizer.apply(
                prepareTimerFor(method,
                        METRIC_NAME_CLIENT_PROCESSING_DURATION,
                        "The total time taken for the client to complete the call, including network delay")));
    }

    @Override
    public <Q, A> ClientCall<Q, A> interceptCall(final MethodDescriptor<Q, A> methodDescriptor,
            final CallOptions callOptions, final Channel channel) {
        final MetricSet metrics = metricsFor(methodDescriptor);
        return new MetricCollectingClientCall<>(
                channel.newCall(methodDescriptor, callOptions),
                this.registry,
                metrics.getRequestCounter(),
                metrics.getResponseCounter(),
                metrics.getTimerFunction());
    }

}

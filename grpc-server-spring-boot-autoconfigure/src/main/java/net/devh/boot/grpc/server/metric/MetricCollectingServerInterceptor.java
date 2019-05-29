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

package net.devh.boot.grpc.server.metric;

import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_PROCESSING_DURATION;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_REQUESTS_RECEIVED;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_RESPONSES_SENT;
import static net.devh.boot.grpc.common.metric.MetricUtils.prepareCounterFor;
import static net.devh.boot.grpc.common.metric.MetricUtils.prepareTimerFor;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.springframework.core.annotation.Order;

import io.grpc.BindableService;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.Status.Code;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.devh.boot.grpc.common.metric.AbstractMetricCollectingInterceptor;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * A gRPC server interceptor that will collect metrics for micrometer.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@GrpcGlobalServerInterceptor
@Order(InterceptorOrder.ORDER_TRACING_METRICS)
public class MetricCollectingServerInterceptor extends AbstractMetricCollectingInterceptor
        implements ServerInterceptor {

    /**
     * Creates a new gRPC server interceptor that will collect metrics into the given {@link MeterRegistry}.
     *
     * @param registry The registry to use.
     */
    public MetricCollectingServerInterceptor(final MeterRegistry registry) {
        super(registry);
    }

    /**
     * Creates a new gRPC server interceptor that will collect metrics into the given {@link MeterRegistry} and uses the
     * given customizer to configure the {@link Counter}s and {@link Timer}s.
     *
     * @param registry The registry to use.
     * @param counterCustomizer The unary function that can be used to customize the created counters.
     * @param timerCustomizer The unary function that can be used to customize the created timers.
     * @param eagerInitializedCodes The status codes that should be eager initialized.
     */
    public MetricCollectingServerInterceptor(final MeterRegistry registry,
            final UnaryOperator<Counter.Builder> counterCustomizer,
            final UnaryOperator<Timer.Builder> timerCustomizer, final Code... eagerInitializedCodes) {
        super(registry, counterCustomizer, timerCustomizer, eagerInitializedCodes);
    }

    /**
     * Pre-registers the all methods provided by the given service. This will initialize all default counters and timers
     * for those methods.
     *
     * @param service The service to initialize the meters for.
     * @see #preregisterService(ServerServiceDefinition)
     */
    public void preregisterService(final BindableService service) {
        preregisterService(service.bindService());
    }

    /**
     * Pre-registers the all methods provided by the given service. This will initialize all default counters and timers
     * for those methods.
     *
     * @param serviceDefinition The service to initialize the meters for.
     * @see #preregisterService(ServiceDescriptor)
     */
    public void preregisterService(final ServerServiceDefinition serviceDefinition) {
        preregisterService(serviceDefinition.getServiceDescriptor());
    }

    @Override
    protected Counter newRequestCounterFor(final MethodDescriptor<?, ?> method) {
        return this.counterCustomizer.apply(
                prepareCounterFor(method,
                        METRIC_NAME_SERVER_REQUESTS_RECEIVED,
                        "The total number of requests received"))
                .register(this.registry);
    }

    @Override
    protected Counter newResponseCounterFor(final MethodDescriptor<?, ?> method) {
        return this.counterCustomizer.apply(
                prepareCounterFor(method,
                        METRIC_NAME_SERVER_RESPONSES_SENT,
                        "The total number of responses sent"))
                .register(this.registry);
    }

    @Override
    protected Function<Code, Timer> newTimerFunction(final MethodDescriptor<?, ?> method) {
        return asTimerFunction(() -> this.timerCustomizer.apply(
                prepareTimerFor(method,
                        METRIC_NAME_SERVER_PROCESSING_DURATION,
                        "The total time taken for the server to process requests as observed by the server")));
    }

    @Override
    public <Q, A> ServerCall.Listener<Q> interceptCall(
            final ServerCall<Q, A> call,
            final Metadata requestHeaders,
            final ServerCallHandler<Q, A> next) {
        final MetricSet metrics = metricsFor(call.getMethodDescriptor());
        final ServerCall<Q, A> monitoringCall = new MetricCollectingServerCall<>(call, this.registry,
                metrics.getResponseCounter(), metrics.getTimerFunction());
        return new MetricCollectingServerCallListener<>(
                next.startCall(monitoringCall, requestHeaders), metrics.getRequestCounter());
    }

}

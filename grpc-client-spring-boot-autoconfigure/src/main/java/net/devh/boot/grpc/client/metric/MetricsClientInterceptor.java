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

package net.devh.boot.grpc.client.metric;

import java.util.function.UnaryOperator;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * A gRPC client interceptor that will collect metrics using StreamTracer.
 */
public class MetricsClientInterceptor extends AbstractMetricClientCollectingInterceptor
        implements ClientInterceptor {

    private static final String METRIC_NAME_CLIENT_ATTEMPT_STARTED = "grpc.client.attempt.started";

    public MetricsClientInterceptor(final MeterRegistry registry) {
        super(registry);
    }

    public MetricsClientInterceptor(final MeterRegistry registry,
            final UnaryOperator<Counter.Builder> counterCustomizer, final UnaryOperator<Timer.Builder> timerCustomizer,
            final Code... eagerInitializedCodes) {
        super(registry, counterCustomizer, timerCustomizer, eagerInitializedCodes);
    }

    @Override
    protected Counter newAttemptCounterFor(final MethodDescriptor<?, ?> method) {
        return this.counterCustomizer
                .apply(prepareCounterFor(method, METRIC_NAME_CLIENT_ATTEMPT_STARTED,
                        "Number of client call attempts started"))
                .register(this.registry);
    }


    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        final MetricSet metrics = metricsFor(method);

        final CallTraceUtil.CallAttemptsTracerFactory tracerFactory =
                new CallTraceUtil.CallAttemptsTracerFactory(method.getFullMethodName(),
                        metrics.getAttemptCounter());

        ClientCall<ReqT, RespT> call =
                next.newCall(method, callOptions.withStreamTracerFactory(tracerFactory));

        return new SimpleForwardingClientCall<ReqT, RespT>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                delegate().start(
                        new SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onClose(Status status, Metadata trailers) {
                                tracerFactory.callEnded(status);
                                super.onClose(status, trailers);
                            }
                        },
                        headers);
            }
        };
    }
}

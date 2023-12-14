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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * A gRPC client interceptor that will collect gRPC metrics.
 */
public class MetricsClientInterceptor implements ClientInterceptor {

    private final MeterRegistry registry;
    private final MetricsCounters metricsCounters;

    public MetricsClientInterceptor(MeterRegistry meterRegistry) {
        this.registry = meterRegistry;
        this.metricsCounters = MetricsClientInstruments.micrometerInstruments(this.registry);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        final MetricsClientStreamTracers.CallAttemptsTracerFactory tracerFactory =
                new MetricsClientStreamTracers.CallAttemptsTracerFactory(method.getFullMethodName(), metricsCounters);

        ClientCall<ReqT, RespT> call =
                next.newCall(method, callOptions.withStreamTracerFactory(tracerFactory));

        return new SimpleForwardingClientCall<ReqT, RespT>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                delegate().start(
                        new SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onClose(Status status, Metadata trailers) {
                                super.onClose(status, trailers);
                            }
                        },
                        headers);
            }
        };
    }
}

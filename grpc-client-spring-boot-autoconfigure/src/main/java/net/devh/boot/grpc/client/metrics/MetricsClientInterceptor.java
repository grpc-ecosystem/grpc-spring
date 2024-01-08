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

package net.devh.boot.grpc.client.metrics;

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
 * A gRPC client interceptor that collects gRPC metrics.
 *
 * <b>Note:</b> This class uses experimental grpc-java-API features.
 */
public class MetricsClientInterceptor implements ClientInterceptor {

    private final MetricsMeters metricsMeters;

    /**
     * Creates a new gRPC client interceptor that collects metrics into the given
     * {@link io.micrometer.core.instrument.MeterRegistry}.
     *
     * @param registry The MeterRegistry to use.
     */
    public MetricsClientInterceptor(MeterRegistry registry) {
        this.metricsMeters = MetricsClientInstruments.newClientMetricsMeters(registry);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        /*
         * This is a per call ClientStreamTracer.Factory which creates a new stream tracer for each attempt under the
         * same call. Each call needs a dedicated factory as they share the same method descriptor.
         */
        final MetricsClientStreamTracers.CallAttemptsTracerFactory tracerFactory =
                new MetricsClientStreamTracers.CallAttemptsTracerFactory(method.getFullMethodName(),
                        metricsMeters);

        ClientCall<ReqT, RespT> call =
                next.newCall(method, callOptions.withStreamTracerFactory(tracerFactory));

        // TODO(dnvindhya): Collect the actual response/error in the SimpleForwardingClientCall
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

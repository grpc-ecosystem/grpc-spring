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

import java.util.function.Supplier;

import com.google.common.base.Stopwatch;

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

    private final MetricsClientMeters metricsClientMeters;
    private final Supplier<Stopwatch> stopwatchSupplier;

    /**
     * Creates a new gRPC client interceptor that collects metrics into the given
     * {@link io.micrometer.core.instrument.MeterRegistry}.
     *
     * @param registry The MeterRegistry to use.
     */
    public MetricsClientInterceptor(MeterRegistry registry, Supplier<Stopwatch> stopwatchSupplier) {
        this(MetricsClientInstruments.newClientMetricsMeters(registry), stopwatchSupplier);
    }

    public MetricsClientInterceptor(MetricsClientMeters meters, Supplier<Stopwatch> stopwatchSupplier) {
        this.metricsClientMeters = meters;
        this.stopwatchSupplier = stopwatchSupplier;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        /*
         * This is a per call ClientStreamTracer.Factory which creates a new stream tracer for each attempt under the
         * same call. Each call needs a dedicated factory as they share the same method descriptor.
         */
        final MetricsClientStreamTracers.CallAttemptsTracerFactory tracerFactory =
                new MetricsClientStreamTracers.CallAttemptsTracerFactory(
                        new MetricsClientStreamTracers(stopwatchSupplier),
                        method.getFullMethodName(),
                        metricsClientMeters);

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

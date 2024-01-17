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

package net.devh.boot.grpc.server.metrics;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;

import io.grpc.Metadata;
import io.grpc.ServerStreamTracer;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Provides factories for {@link io.grpc.StreamTracer} that records metrics.
 *
 * <p>
 * On the server-side, there is only one ServerStream per each ServerCall, and ServerStream starts earlier than the
 * ServerCall. Therefore, only one tracer is created per stream/call and it's the tracer that reports the metrics
 * summary.
 *
 * <b>Note:</b> This class uses experimental grpc-java-API features.
 */
public final class MetricsServerStreamTracers {
    private final Supplier<Stopwatch> stopwatchSupplier;

    public MetricsServerStreamTracers(Supplier<Stopwatch> stopwatchSupplier) {
        this.stopwatchSupplier = checkNotNull(stopwatchSupplier, "stopwatchSupplier");
    }

    /**
     * Returns the server stream tracer factory for metrics.
     */
    public ServerStreamTracer.Factory getMetricsServerTracerFactory(MeterRegistry registry) {
        return new MetricsServerTracerFactory(registry);
    }

    private static final class ServerTracer extends ServerStreamTracer {
        private final MetricsServerStreamTracers tracer;
        private final String fullMethodName;
        private final MetricsServerMeters metricsServerMeters;
        private final Stopwatch stopwatch;
        private static final AtomicLongFieldUpdater<ServerTracer> outboundWireSizeUpdater =
                AtomicLongFieldUpdater.newUpdater(ServerTracer.class, "outboundWireSize");
        private static final AtomicLongFieldUpdater<ServerTracer> inboundWireSizeUpdater =
                AtomicLongFieldUpdater.newUpdater(ServerTracer.class, "inboundWireSize");
        private static final AtomicIntegerFieldUpdater<ServerTracer> streamClosedUpdater =
                AtomicIntegerFieldUpdater.newUpdater(ServerTracer.class, "streamClosed");
        private volatile long outboundWireSize;
        private volatile long inboundWireSize;
        private volatile int streamClosed;


        ServerTracer(MetricsServerStreamTracers tracer, String fullMethodName, MetricsServerMeters meters) {
            this.tracer = checkNotNull(tracer, "tracer");
            this.fullMethodName = fullMethodName;
            this.metricsServerMeters = meters;
            // start stopwatch
            this.stopwatch = tracer.stopwatchSupplier.get().start();
        }

        @Override
        public void serverCallStarted(ServerCallInfo<?, ?> callInfo) {
            this.metricsServerMeters.getServerCallCounter()
                    .withTags(Tags.of("grpc.method", this.fullMethodName))
                    .increment();
        }

        @Override
        public void outboundWireSize(long bytes) {
            outboundWireSizeUpdater.getAndAdd(this, bytes);
        }

        @Override
        public void inboundWireSize(long bytes) {
            inboundWireSizeUpdater.getAndAdd(this, bytes);
        }

        @Override
        public void streamClosed(Status status) {
            if (streamClosedUpdater.getAndSet(this, 1) != 0) {
                return;
            }
            long callLatencyNanos = stopwatch.elapsed(TimeUnit.NANOSECONDS);

            Tags serverMetricTags =
                    Tags.of("grpc.method", this.fullMethodName, "grpc.status", status.getCode().toString());
            this.metricsServerMeters.getServerCallDuration()
                    .withTags(serverMetricTags)
                    .record(callLatencyNanos, TimeUnit.NANOSECONDS);
            // .record(callLatencyNanos * SECONDS_PER_NANO, TimeUnit.SECONDS);
            this.metricsServerMeters.getSentMessageSizeDistribution()
                    .withTags(serverMetricTags)
                    .record(outboundWireSize);
            this.metricsServerMeters.getReceivedMessageSizeDistribution()
                    .withTags(serverMetricTags)
                    .record(inboundWireSize);
        }
    }

    final class MetricsServerTracerFactory extends ServerStreamTracer.Factory {

        private final MetricsServerMeters metricsServerMeters;

        MetricsServerTracerFactory(MeterRegistry registry) {
            this.metricsServerMeters = MetricsServerInstruments.newServerMetricsMeters(registry);
        }

        @Override
        public ServerStreamTracer newServerStreamTracer(String fullMethodName, Metadata headers) {
            return new ServerTracer(MetricsServerStreamTracers.this, fullMethodName, this.metricsServerMeters);
        }
    }

}

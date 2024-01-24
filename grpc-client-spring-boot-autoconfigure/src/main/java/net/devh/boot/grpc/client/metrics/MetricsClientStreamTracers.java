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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Supplier;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Stopwatch;

import io.grpc.ClientStreamTracer;
import io.grpc.ClientStreamTracer.StreamInfo;
import io.grpc.Deadline;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.micrometer.core.instrument.Tags;

/**
 * Provides factories for {@link io.grpc.StreamTracer} that records metrics.
 *
 * <p>
 * On the client-side, a factory is created for each call, and the factory creates a stream tracer for each attempt.
 *
 * <b>Note:</b> This class uses experimental grpc-java-API features.
 */
final class MetricsClientStreamTracers {
    private static final Supplier<Stopwatch> STOPWATCH_SUPPLIER = Stopwatch::createUnstarted;
    private final Supplier<Stopwatch> stopwatchSupplier;

    public MetricsClientStreamTracers() {
        this(STOPWATCH_SUPPLIER);
    }

    public MetricsClientStreamTracers(Supplier<Stopwatch> stopwatchSupplier) {
        this.stopwatchSupplier = checkNotNull(stopwatchSupplier, "stopwatchSupplier");
    }

    private static final class ClientTracer extends ClientStreamTracer {
        private final MetricsClientStreamTracers tracerModule;
        private final CallAttemptsTracerFactory attemptsState;
        private final MetricsClientMeters metricsClientMeters;
        private static final AtomicLongFieldUpdater<ClientTracer> outboundWireSizeUpdater =
                AtomicLongFieldUpdater.newUpdater(ClientTracer.class, "outboundWireSize");
        private static final AtomicLongFieldUpdater<ClientTracer> inboundWireSizeUpdater =
                AtomicLongFieldUpdater.newUpdater(ClientTracer.class, "inboundWireSize");
        private volatile long outboundWireSize;
        private volatile long inboundWireSize;
        private final StreamInfo info;
        private final String fullMethodName;
        final AtomicBoolean inboundReceivedOrClosed = new AtomicBoolean();
        final Stopwatch stopwatch;
        Code statusCode;
        long attemptNanos;

        ClientTracer(CallAttemptsTracerFactory attemptsState, MetricsClientStreamTracers tracers,
                StreamInfo info,
                String fullMethodName, MetricsClientMeters metricsClientMeters) {
            this.attemptsState = attemptsState;
            this.tracerModule = tracers;
            this.info = info;
            this.fullMethodName = fullMethodName;
            this.metricsClientMeters = metricsClientMeters;
            this.stopwatch = tracers.stopwatchSupplier.get().start();
        }

        @Override
        public void outboundWireSize(long bytes) {
            outboundWireSizeUpdater.getAndAdd(this, bytes);
        }

        @Override
        public void inboundWireSize(long bytes) {
            inboundWireSizeUpdater.getAndAdd(this, bytes);
        }

        public void inboundMessage(int seqNo) {
            if (inboundReceivedOrClosed.compareAndSet(false, true)) {
                // Because inboundUncompressedSize() might be called after streamClosed(),
                // we will report stats in callEnded(). Note that this attempt is already committed.
                attemptsState.inboundMetricTracer = this;
            }
        }

        @Override
        public void streamClosed(Status status) {
            stopwatch.stop();
            attemptNanos = stopwatch.elapsed(TimeUnit.NANOSECONDS);
            Deadline deadline = info.getCallOptions().getDeadline();
            statusCode = status.getCode();
            if (statusCode == Status.Code.CANCELLED && deadline != null) {
                // When the server's deadline expires, it can only reset the stream with CANCEL and no
                // description. Since our timer may be delayed in firing, we double-check the deadline and
                // turn the failure into the likely more helpful DEADLINE_EXCEEDED status.
                if (deadline.isExpired()) {
                    statusCode = Code.DEADLINE_EXCEEDED;
                }
            }
            attemptsState.attemptEnded();
            if (inboundReceivedOrClosed.compareAndSet(false, true)) {
                // Stream is closed early. So no need to record metrics for any inbound events after this
                // point.
                recordFinishedAttempt();
            } // Otherwise will report metrics in callEnded() to guarantee all inbound metrics are
              // recorded.
        }

        void recordFinishedAttempt() {
            Tags attemptMetricTags =
                    Tags.of("grpc.method", fullMethodName, "grpc.status", statusCode.toString());
            this.metricsClientMeters.getClientAttemptDuration()
                    .withTags(attemptMetricTags)
                    .record(attemptNanos, TimeUnit.NANOSECONDS);
            this.metricsClientMeters.getSentMessageSizeDistribution()
                    .withTags(attemptMetricTags)
                    .record(outboundWireSize);
            this.metricsClientMeters.getReceivedMessageSizeDistribution()
                    .withTags(attemptMetricTags)
                    .record(inboundWireSize);
        }
    }

    static final class CallAttemptsTracerFactory extends ClientStreamTracer.Factory {
        ClientTracer inboundMetricTracer;
        private final MetricsClientStreamTracers tracerModule;
        private final MetricsClientMeters metricsClientMeters;
        private final Stopwatch attemptStopwatch;
        private final Stopwatch clientCallStopWatch;
        private final String fullMethodName;
        private final Object lock = new Object();
        private final AtomicLong attemptsPerCall = new AtomicLong();
        private long callLatencyNanos;
        private Status status;
        @GuardedBy("lock")
        private boolean callEnded;
        @GuardedBy("lock")
        private int activeStreams;
        @GuardedBy("lock")
        private boolean finishedCallToBeRecorded;

        CallAttemptsTracerFactory(MetricsClientStreamTracers tracerModule, String fullMethodName,
                MetricsClientMeters metricsClientMeters) {
            this.tracerModule = checkNotNull(tracerModule, "tracerModule");
            this.fullMethodName = checkNotNull(fullMethodName, "fullMethodName");
            this.metricsClientMeters = checkNotNull(metricsClientMeters, "metricsMeters");
            this.attemptStopwatch = tracerModule.stopwatchSupplier.get();
            this.clientCallStopWatch = tracerModule.stopwatchSupplier.get().start();

            // Record here in case newClientStreamTracer() would never be called.
            this.metricsClientMeters.getAttemptCounter()
                    .withTags(Tags.of("grpc.method", fullMethodName))
                    .increment();
        }

        @Override
        public ClientStreamTracer newClientStreamTracer(StreamInfo info, Metadata metadata) {
            synchronized (lock) {
                if (finishedCallToBeRecorded) {
                    // This can be the case when the called is cancelled but a retry attempt is created.
                    return new ClientStreamTracer() {};
                }
                if (++activeStreams == 1 && attemptStopwatch.isRunning()) {
                    attemptStopwatch.stop();
                }
            }
            // Skip recording for the first time, since it is already recorded in
            // CallAttemptsTracerFactory constructor. attemptsPerCall will be non-zero after the first
            // attempt, as first attempt cannot be a transparent retry.
            if (attemptsPerCall.get() > 0) {
                this.metricsClientMeters.getAttemptCounter()
                        .withTags((Tags.of("grpc.method", fullMethodName)))
                        .increment();
            }
            if (!info.isTransparentRetry()) {
                attemptsPerCall.incrementAndGet();
            }

            return new ClientTracer(this, tracerModule, info, fullMethodName, metricsClientMeters);
        }

        // Called when each attempt is ended
        void attemptEnded() {
            boolean shouldRecordFinishedCall = false;
            synchronized (lock) {
                if (--activeStreams == 0) {
                    attemptStopwatch.start();
                    if (callEnded && !finishedCallToBeRecorded) {
                        shouldRecordFinishedCall = true;
                        finishedCallToBeRecorded = true;
                    }
                }
            }
            if (shouldRecordFinishedCall) {
                recordFinishedCall();
            }
        }

        void callEnded(Status status) {
            clientCallStopWatch.stop();
            this.status = status;
            boolean shouldRecordFinishedCall = false;
            synchronized (lock) {
                if (callEnded) {
                    return;
                }
                callEnded = true;
                if (activeStreams == 0 && !finishedCallToBeRecorded) {
                    shouldRecordFinishedCall = true;
                    finishedCallToBeRecorded = true;
                }
            }
            if (shouldRecordFinishedCall) {
                recordFinishedCall();
            }
        }

        void recordFinishedCall() {
            if (attemptsPerCall.get() == 0) {
                ClientTracer tracer = new ClientTracer(this, tracerModule, null, fullMethodName,
                        metricsClientMeters);
                tracer.attemptNanos = attemptStopwatch.elapsed(TimeUnit.NANOSECONDS);
                tracer.statusCode = status.getCode();
                tracer.recordFinishedAttempt();
            } else if (inboundMetricTracer != null) {
                // activeStreams has been decremented to 0 by attemptEnded(),
                // so inboundMetricTracer.statusCode is guaranteed to be assigned already.
                inboundMetricTracer.recordFinishedAttempt();
            }
            callLatencyNanos = clientCallStopWatch.elapsed(TimeUnit.NANOSECONDS);
            Tags clientCallMetricTags =
                    Tags.of("grpc.method", this.fullMethodName, "grpc.status", status.getCode().toString());
            this.metricsClientMeters.getClientCallDuration()
                    .withTags(clientCallMetricTags)
                    .record(callLatencyNanos, TimeUnit.NANOSECONDS);
        }
    }
}

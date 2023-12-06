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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;

import io.grpc.ClientStreamTracer;
import io.grpc.ClientStreamTracer.StreamInfo;
import io.grpc.Deadline;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.micrometer.core.instrument.Counter;

public class CallTraceUtil {
    private static final Supplier<Stopwatch> STOPWATCH_SUPPLIER = new Supplier<Stopwatch>() {
        @Override
        public Stopwatch get() {
            return Stopwatch.createUnstarted();
        }
    };

    private static final class ClientTracer extends ClientStreamTracer {
        private static final AtomicLongFieldUpdater<ClientTracer> outboundWireSizeUpdater =
                AtomicLongFieldUpdater.newUpdater(ClientTracer.class, "outboundWireSize");
        private static final AtomicLongFieldUpdater<ClientTracer> inboundWireSizeUpdater =
                AtomicLongFieldUpdater.newUpdater(ClientTracer.class, "inboundWireSize");

        final Stopwatch stopwatch;
        final CallAttemptsTracerFactory attemptsState;
        final AtomicBoolean inboundReceivedOrClosed = new AtomicBoolean();
        final StreamInfo info;
        final String fullMethodName;
        volatile long outboundWireSize;
        volatile long inboundWireSize;
        long attemptNanos;
        Code statusCode;

        ClientTracer(CallAttemptsTracerFactory attemptsState, StreamInfo info, String fullMethodName) {
            this.attemptsState = attemptsState;
            this.info = info;
            this.fullMethodName = fullMethodName;
            this.stopwatch = STOPWATCH_SUPPLIER.get().start();
        }

        @Override
        @SuppressWarnings("NonAtomicVolatileUpdate")
        public void outboundWireSize(long bytes) {
            outboundWireSizeUpdater.getAndAdd(this, bytes);
        }

        @Override
        @SuppressWarnings("NonAtomicVolatileUpdate")
        public void inboundWireSize(long bytes) {
            inboundWireSizeUpdater.getAndAdd(this, bytes);
        }

        @Override
        @SuppressWarnings("NonAtomicVolatileUpdate")
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
            if (statusCode == Code.CANCELLED && deadline != null) {
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

            // record attempt duration
            // record sent compressed message size
            // record received compressed message size
        }
    }

    static final class CallAttemptsTracerFactory extends ClientStreamTracer.Factory {
        ClientTracer inboundMetricTracer;
        private final Stopwatch attemptStopwatch;
        private final Stopwatch callStopWatch;
        @GuardedBy("lock")
        private boolean callEnded;
        private final String fullMethodName;
        private Status status;
        private long callLatencyNanos;
        private final Object lock = new Object();
        private final AtomicLong attemptsPerCall = new AtomicLong();
        @GuardedBy("lock")
        private int activeStreams;
        @GuardedBy("lock")
        private boolean finishedCallToBeRecorded;

        private final Counter attemptCounter;

        CallAttemptsTracerFactory(String fullMethodName,
                final Counter attemptCounter) {
            this.fullMethodName = checkNotNull(fullMethodName, "fullMethodName");
            this.attemptStopwatch = STOPWATCH_SUPPLIER.get();
            this.callStopWatch = STOPWATCH_SUPPLIER.get().start();
            this.attemptCounter = checkNotNull(attemptCounter, "attemptCounter");

            // Record here in case mewClientStreamTracer() would never be called.
            this.attemptCounter.increment();
        }

        @Override
        public ClientStreamTracer newClientStreamTracer(StreamInfo info, Metadata metadata) {
            synchronized (lock) {
                if (finishedCallToBeRecorded) {
                    // This can be the case when the call is cancelled but a retry attempt is created.
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
                this.attemptCounter.increment();
            }
            if (!info.isTransparentRetry()) {
                attemptsPerCall.incrementAndGet();
            }
            return new ClientTracer(this, info, fullMethodName);
        }

        // Called whenever each attempt is ended.
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
            callStopWatch.stop();
            this.status = status;
            boolean shouldRecordFinishedCall = false;
            synchronized (lock) {
                if (callEnded) {
                    // TODO(https://github.com/grpc/grpc-java/issues/7921): this shouldn't happen
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
                ClientTracer tracer = new ClientTracer(this, null, fullMethodName);
                tracer.attemptNanos = attemptStopwatch.elapsed(TimeUnit.NANOSECONDS);
                tracer.statusCode = status.getCode();
                tracer.recordFinishedAttempt();
            } else if (inboundMetricTracer != null) {
                // activeStreams has been decremented to 0 by attemptEnded(),
                // so inboundMetricTracer.statusCode is guaranteed to be assigned already.
                inboundMetricTracer.recordFinishedAttempt();
            }
            callLatencyNanos = callStopWatch.elapsed(TimeUnit.NANOSECONDS);
            // record call duration
        }
    }
}

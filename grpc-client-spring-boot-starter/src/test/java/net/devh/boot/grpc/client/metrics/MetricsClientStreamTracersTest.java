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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.grpc.CallOptions;
import io.grpc.ClientStreamTracer;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.devh.boot.grpc.client.metrics.MetricsClientStreamTracers.CallAttemptsTracerFactory;
import net.devh.boot.grpc.common.util.Constants;

/**
 * Tests for {@link MetricsClientStreamTracers}.
 */
class MetricsClientStreamTracersTest {
    private static final CallOptions.Key<String> CUSTOM_OPTION =
            CallOptions.Key.createWithDefault("option1", "default");
    private static final CallOptions CALL_OPTIONS =
            CallOptions.DEFAULT.withOption(CUSTOM_OPTION, "customvalue");
    private static final ClientStreamTracer.StreamInfo STREAM_INFO =
            ClientStreamTracer.StreamInfo.newBuilder().setCallOptions(CALL_OPTIONS).build();

    private static final String CLIENT_ATTEMPT_STARTED = "grpc.client.attempt.started";
    private static final String CLIENT_ATTEMPT_SENT_COMPRESSED_MESSAGE_SIZE =
            "grpc.client.attempt.sent_total_compressed_message_size";
    private static final String CLIENT_ATTEMPT_RECEIVED_COMPRESSED_MESSAGE_SIZE =
            "grpc.client.attempt.rcvd_total_compressed_message_size";
    private static final String CLIENT_ATTEMPT_DURATION =
            "grpc.client.attempt.duration";
    private static final String CLIENT_CALL_DURATION =
            "grpc.client.call.duration";
    private static final String GRPC_METHOD_TAG_KEY = "grpc.method";
    private static final String GRPC_STATUS_TAG_KEY = "grpc.status";
    private static final String FULL_METHOD_NAME = "package1.service1/method1";
    private static final String INSTRUMENTATION_SOURCE_TAG_KEY = "instrumentation_source";
    private static final String INSTRUMENTATION_SOURCE_TAG_VALUE = Constants.LIBRARY_NAME;
    private static final String INSTRUMENTATION_VERSION_TAG_KEY = "instrumentation_version";
    private static final String INSTRUMENTATION_VERSION_TAG_VALUE = Constants.VERSION;

    private static class StringInputStream extends InputStream {
        final String string;

        StringInputStream(String string) {
            this.string = string;
        }

        @Override
        public int read() {
            // InProcessTransport doesn't actually read bytes from the InputStream. The InputStream is
            // passed to the InProcess server and consumed by MARSHALLER.parse().
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    private static final MethodDescriptor.Marshaller<String> MARSHALLER =
            new MethodDescriptor.Marshaller<String>() {
                @Override
                public InputStream stream(String value) {
                    return new StringInputStream(value);
                }

                @Override
                public String parse(InputStream stream) {
                    return ((StringInputStream) stream).string;
                }
            };
    private final MethodDescriptor<String, String> method =
            MethodDescriptor.<String, String>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNKNOWN)
                    .setRequestMarshaller(MARSHALLER)
                    .setResponseMarshaller(MARSHALLER)
                    .setFullMethodName(FULL_METHOD_NAME)
                    .build();

    private FakeClock fakeClock;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        fakeClock = new FakeClock();
        meterRegistry = new SimpleMeterRegistry();
        Metrics.globalRegistry.add(meterRegistry);
    }

    @AfterEach
    void tearDown() {
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
    }

    @Test
    void clientBasicMetrics() {
        MetricsClientStreamTracers module =
                new MetricsClientStreamTracers(fakeClock.getStopwatchSupplier());
        MetricsClientMeters clientMeters = MetricsClientInstruments.newClientMetricsMeters(meterRegistry);
        MetricsClientStreamTracers.CallAttemptsTracerFactory callAttemptsTracerFactory =
                new CallAttemptsTracerFactory(module, method.getFullMethodName(), clientMeters);
        ClientStreamTracer tracer =
                callAttemptsTracerFactory.newClientStreamTracer(STREAM_INFO, new Metadata());

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(1);

        fakeClock.forwardTime(30, MILLISECONDS);
        tracer.outboundHeaders();
        tracer.outboundMessage(0);
        tracer.outboundWireSize(1028);

        fakeClock.forwardTime(100, MILLISECONDS);
        tracer.outboundMessage(1);
        tracer.outboundWireSize(99);

        fakeClock.forwardTime(24, MILLISECONDS);
        tracer.inboundMessage(0);
        tracer.inboundMessage(1);
        tracer.inboundWireSize(111);
        tracer.streamClosed(Status.OK);
        callAttemptsTracerFactory.callEnded(Status.OK);

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(1);

        Tags expectedTags =
                Tags.of(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME,
                        GRPC_STATUS_TAG_KEY, Status.Code.OK.toString(),
                        INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE,
                        INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE);

        HistogramSnapshot attemptDurationSnapshot = meterRegistry.get(CLIENT_ATTEMPT_DURATION)
                .tags(expectedTags)
                .timer()
                .takeSnapshot();
        HistogramSnapshot attemptDurationHistogram = HistogramSnapshot.empty(1L, 154L, 1.54E8);
        verifyHistogramSnapshot(true, attemptDurationSnapshot, attemptDurationHistogram,
                new CountAtBucket(1.6E8, 1));

        HistogramSnapshot callDurationSnapshot = meterRegistry.get(CLIENT_CALL_DURATION)
                .tags(expectedTags)
                .timer()
                .takeSnapshot();
        HistogramSnapshot expectedCallDurationHistogram = HistogramSnapshot.empty(1L, 154L, 1.54E8);
        verifyHistogramSnapshot(true, callDurationSnapshot, expectedCallDurationHistogram,
                new CountAtBucket(1.6E8, 1));

        HistogramSnapshot sentAttemptMessageSizeSnapShot =
                meterRegistry.get(CLIENT_ATTEMPT_SENT_COMPRESSED_MESSAGE_SIZE)
                        .tags(expectedTags)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot expectedAttemptSentMessageSizeHistogram = HistogramSnapshot.empty(1L, 1127L, 1127L);
        verifyHistogramSnapshot(false, sentAttemptMessageSizeSnapShot, expectedAttemptSentMessageSizeHistogram,
                new CountAtBucket(2048.0, 1));

        HistogramSnapshot receivedAttemptMessageSizeSnapShot =
                meterRegistry.get(CLIENT_ATTEMPT_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                        .tags(expectedTags)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot expectedAttemptReceivedMessageSizeHistogram = HistogramSnapshot.empty(1L, 111L, 111L);
        verifyHistogramSnapshot(false, receivedAttemptMessageSizeSnapShot, expectedAttemptReceivedMessageSizeHistogram,
                new CountAtBucket(1024.0, 1));
    }

    // This test is only unit-testing teh metrics recording logic. Retry behavior is faked.
    @Test
    void recordAttemptMetrics() {
        MetricsClientStreamTracers module =
                new MetricsClientStreamTracers(fakeClock.getStopwatchSupplier());
        MetricsClientMeters clientMeters = MetricsClientInstruments.newClientMetricsMeters(meterRegistry);
        MetricsClientStreamTracers.CallAttemptsTracerFactory callAttemptsTracerFactory =
                new CallAttemptsTracerFactory(module, method.getFullMethodName(), clientMeters);
        ClientStreamTracer tracer =
                callAttemptsTracerFactory.newClientStreamTracer(STREAM_INFO, new Metadata());

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(1);

        fakeClock.forwardTime(60, MILLISECONDS);
        tracer.outboundHeaders();
        fakeClock.forwardTime(120, MILLISECONDS);
        tracer.outboundMessage(0);
        tracer.outboundMessage(1);
        tracer.outboundWireSize(1028);
        fakeClock.forwardTime(24, MILLISECONDS);
        tracer.streamClosed(Status.UNAVAILABLE);

        Tags expectedUnailableStatusTags =
                Tags.of(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME,
                        GRPC_STATUS_TAG_KEY, Status.Code.UNAVAILABLE.toString(),
                        INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE,
                        INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE);

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(1);
        assertThat(meterRegistry.get(CLIENT_ATTEMPT_DURATION)
                .tags(expectedUnailableStatusTags)
                .timer()
                .takeSnapshot()
                .total(MILLISECONDS)).isEqualTo(60L + 120L + 24L);
        assertThat(meterRegistry.get(CLIENT_ATTEMPT_SENT_COMPRESSED_MESSAGE_SIZE)
                .tags(expectedUnailableStatusTags)
                .summary()
                .takeSnapshot()
                .total()).isEqualTo(1028L);
        assertThat(meterRegistry.get(CLIENT_ATTEMPT_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                .tags(expectedUnailableStatusTags)
                .summary()
                .takeSnapshot()
                .total()).isEqualTo(0);

        // Faking retry
        fakeClock.forwardTime(1200, MILLISECONDS);

        tracer = callAttemptsTracerFactory.newClientStreamTracer(STREAM_INFO, new Metadata());

        tracer.outboundHeaders();
        tracer.outboundMessage(0);
        tracer.outboundMessage(1);
        tracer.outboundWireSize(1028);
        fakeClock.forwardTime(100, MILLISECONDS);
        tracer.streamClosed(Status.NOT_FOUND);

        Tags expectedNotFoundStatusTags =
                Tags.of(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME,
                        GRPC_STATUS_TAG_KEY, Status.Code.NOT_FOUND.toString(),
                        INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE,
                        INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE);

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(2);

        HistogramSnapshot secondAttemptDurationSnapshot = meterRegistry.get(CLIENT_ATTEMPT_DURATION)
                .tags(expectedNotFoundStatusTags)
                .timer()
                .takeSnapshot();
        HistogramSnapshot secondExpectedAttemptDurationHistogram = HistogramSnapshot.empty(1L, 100L, 1.0E8);
        verifyHistogramSnapshot(true, secondAttemptDurationSnapshot, secondExpectedAttemptDurationHistogram,
                new CountAtBucket(1.0E8, 1));

        HistogramSnapshot secondSentAttemptMessageSizeSnapShot =
                meterRegistry.get(CLIENT_ATTEMPT_SENT_COMPRESSED_MESSAGE_SIZE)
                        .tags(expectedNotFoundStatusTags)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot secondExpectedAttemptSentMessageSizeHistogram = HistogramSnapshot.empty(1L, 1028L, 1028.0);
        verifyHistogramSnapshot(false, secondSentAttemptMessageSizeSnapShot,
                secondExpectedAttemptSentMessageSizeHistogram,
                new CountAtBucket(2048.0, 1));

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                .tags(expectedNotFoundStatusTags)
                .summary()
                .takeSnapshot()
                .total()).isEqualTo(0);

        // fake transparent retry
        fakeClock.forwardTime(100, MILLISECONDS);

        tracer = callAttemptsTracerFactory.newClientStreamTracer(
                STREAM_INFO.toBuilder().setIsTransparentRetry(true).build(), new Metadata());

        fakeClock.forwardTime(32, MILLISECONDS);
        tracer.streamClosed(Status.UNAVAILABLE);

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(3);

        HistogramSnapshot thirdAttemptDurationSnapshot = meterRegistry.get(CLIENT_ATTEMPT_DURATION)
                .tags(expectedUnailableStatusTags)
                .timer()
                .takeSnapshot();
        HistogramSnapshot thirdExpectedAttemptDurationHistogram = HistogramSnapshot.empty(2L, 204L + 32L, 2.04E8);
        verifyHistogramSnapshot(true, thirdAttemptDurationSnapshot, thirdExpectedAttemptDurationHistogram,
                new CountAtBucket(4.0E7, 1));

        HistogramSnapshot thirdSentAttemptMessageSizeSnapShot =
                meterRegistry.get(CLIENT_ATTEMPT_SENT_COMPRESSED_MESSAGE_SIZE)
                        .tags(expectedUnailableStatusTags)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot thirdExpectedAttemptSentMessageSizeHistogram = HistogramSnapshot.empty(2L, 1028L + 0, 1028.0);
        verifyHistogramSnapshot(false, thirdSentAttemptMessageSizeSnapShot,
                thirdExpectedAttemptSentMessageSizeHistogram,
                new CountAtBucket(2048.0, 2));

        HistogramSnapshot thirdReceivedAttemptMessageSizeSnapShot =
                meterRegistry.get(CLIENT_ATTEMPT_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                        .tags(expectedUnailableStatusTags)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot thirdExpectedAttemptReceivedMessageSizeHistogram = HistogramSnapshot.empty(2L, 0, 0);
        verifyHistogramSnapshot(false, thirdReceivedAttemptMessageSizeSnapShot,
                thirdExpectedAttemptReceivedMessageSizeHistogram,
                new CountAtBucket(1024.0, 2));

        // Fake another transparent retry
        fakeClock.forwardTime(10, MILLISECONDS);

        tracer = callAttemptsTracerFactory.newClientStreamTracer(
                STREAM_INFO.toBuilder().setIsTransparentRetry(true).build(), new Metadata());

        tracer.outboundHeaders();
        tracer.outboundMessage(0);
        tracer.outboundMessage(1);
        tracer.outboundWireSize(1028);

        fakeClock.forwardTime(124, MILLISECONDS);
        tracer.inboundMessage(0);
        tracer.inboundWireSize(33);

        fakeClock.forwardTime(24, MILLISECONDS);
        // RPC succeeded
        tracer.streamClosed(Status.OK);
        callAttemptsTracerFactory.callEnded(Status.OK);

        Tags expectedOKStatusTags =
                Tags.of(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME,
                        GRPC_STATUS_TAG_KEY, Status.Code.OK.toString(),
                        INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE,
                        INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE);

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(4);
        assertThat(meterRegistry.get(CLIENT_ATTEMPT_DURATION)
                .tags(expectedOKStatusTags)
                .timer()
                .takeSnapshot()
                .total(MILLISECONDS)).isEqualTo(124L + 24L);
        assertThat(meterRegistry.get(CLIENT_ATTEMPT_SENT_COMPRESSED_MESSAGE_SIZE)
                .tags(expectedOKStatusTags)
                .summary()
                .takeSnapshot()
                .total()).isEqualTo(1028L);
        assertThat(meterRegistry.get(CLIENT_ATTEMPT_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                .tags(expectedOKStatusTags)
                .summary()
                .takeSnapshot()
                .total()).isEqualTo(33);

        HistogramSnapshot callDurationSnapshot = meterRegistry.get(CLIENT_CALL_DURATION)
                .tags(expectedOKStatusTags)
                .timer()
                .takeSnapshot();
        HistogramSnapshot expectedCallDurationHistogram =
                HistogramSnapshot.empty(1L, 60 + 120 + 24 + 1200 + 100 + 100 + 32 + 10 + 124 + 24L, 1.794E9);
        verifyHistogramSnapshot(true, callDurationSnapshot, expectedCallDurationHistogram,
                new CountAtBucket(2.0E9, 1));
    }

    @Test
    void clientStreamNeverCreatedStillRecordMetrics() {
        MetricsClientStreamTracers module =
                new MetricsClientStreamTracers(fakeClock.getStopwatchSupplier());
        MetricsClientMeters clientMeters = MetricsClientInstruments.newClientMetricsMeters(meterRegistry);
        MetricsClientStreamTracers.CallAttemptsTracerFactory callAttemptsTracerFactory =
                new CallAttemptsTracerFactory(module, method.getFullMethodName(), clientMeters);

        fakeClock.forwardTime(3000, MILLISECONDS);
        Status status = Status.DEADLINE_EXCEEDED.withDescription("5 seconds");

        callAttemptsTracerFactory.callEnded(status);

        Tags expectedDeadlineExceededStatusTags =
                Tags.of(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME,
                        GRPC_STATUS_TAG_KEY, Status.Code.DEADLINE_EXCEEDED.toString(),
                        INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE,
                        INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE);

        HistogramSnapshot attemptDurationSnapshot = meterRegistry.get(CLIENT_ATTEMPT_DURATION)
                .tags(expectedDeadlineExceededStatusTags)
                .timer()
                .takeSnapshot();
        HistogramSnapshot expectedAttemptDurationHistogram = HistogramSnapshot.empty(1L, 0, 0);
        verifyHistogramSnapshot(true, attemptDurationSnapshot, expectedAttemptDurationHistogram,
                new CountAtBucket(10000.0, 1));

        HistogramSnapshot sentAttemptMessageSizeSnapShot =
                meterRegistry.get(CLIENT_ATTEMPT_SENT_COMPRESSED_MESSAGE_SIZE)
                        .tags(expectedDeadlineExceededStatusTags)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot expectedAttemptSentMessageSizeHistogram = HistogramSnapshot.empty(1L, 0, 0);
        verifyHistogramSnapshot(false, sentAttemptMessageSizeSnapShot, expectedAttemptSentMessageSizeHistogram,
                new CountAtBucket(1024.0, 1));

        HistogramSnapshot receivedAttemptMessageSizeSnapShot =
                meterRegistry.get(CLIENT_ATTEMPT_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                        .tags(expectedDeadlineExceededStatusTags)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot expectedAttemptReceivedMessageSizeHistogram = HistogramSnapshot.empty(1L, 0, 0);
        verifyHistogramSnapshot(false, receivedAttemptMessageSizeSnapShot, expectedAttemptReceivedMessageSizeHistogram,
                new CountAtBucket(1024.0, 1));

        HistogramSnapshot callDurationSnapshot = meterRegistry.get(CLIENT_CALL_DURATION)
                .tags(expectedDeadlineExceededStatusTags)
                .timer()
                .takeSnapshot();
        HistogramSnapshot expectedCallDurationHistogram = HistogramSnapshot.empty(1L, 3000, 3.0E9);
        verifyHistogramSnapshot(true, callDurationSnapshot, expectedCallDurationHistogram,
                new CountAtBucket(5.0E9, 1));
    }

    static void verifyHistogramSnapshot(boolean isTimer, HistogramSnapshot actual, HistogramSnapshot expected,
            CountAtBucket expectedHistogramBucketWithValue) {
        if (isTimer) {
            assertThat(actual.total(MILLISECONDS)).isEqualTo(expected.total());
        } else {
            assertThat(actual.total()).isEqualTo(expected.total());
        }
        assertThat(actual.count()).isEqualTo(expected.count());
        assertThat(actual.max()).isEqualTo(expected.max());
        assertThat(actual.histogramCounts()).contains(expectedHistogramBucketWithValue);
    }
}

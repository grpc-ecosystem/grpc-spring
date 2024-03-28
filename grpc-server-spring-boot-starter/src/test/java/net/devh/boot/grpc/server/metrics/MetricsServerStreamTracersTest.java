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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import javax.annotation.Nullable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerStreamTracer;
import io.grpc.ServerStreamTracer.ServerCallInfo;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.devh.boot.grpc.common.util.Constants;

/**
 * Tests for {@link MetricsServerStreamTracers}.
 */
class MetricsServerStreamTracersTest {

    private static final String SERVER_CALL_STARTED = "grpc.server.call.started";
    private static final String SERVER_SENT_COMPRESSED_MESSAGE_SIZE =
            "grpc.server.call.sent_total_compressed_message_size";
    private static final String SERVER_RECEIVED_COMPRESSED_MESSAGE_SIZE =
            "grpc.server.call.rcvd_total_compressed_message_size";
    private static final String SERVER_CALL_DURATION =
            "grpc.server.call.duration";
    private static final String FULL_METHOD_NAME = "package1.service1/method1";
    private static final String GRPC_METHOD_TAG_KEY = "grpc.method";
    private static final String GRPC_STATUS_TAG_KEY = "grpc.status";
    private static final String INSTRUMENTATION_SOURCE_TAG_KEY = "instrumentation_source";
    private static final String INSTRUMENTATION_SOURCE_TAG_VALUE = Constants.INSTRUMENTATION_SOURCE_TAG_VALUE;
    private static final String INSTRUMENTATION_VERSION_TAG_KEY = "instrumentation_version";
    private static final String INSTRUMENTATION_VERSION_TAG_VALUE = Constants.PROJECT_VERSION;


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

    static class CallInfo<ReqT, RespT> extends ServerCallInfo<ReqT, RespT> {
        private final MethodDescriptor<ReqT, RespT> methodDescriptor;
        private final Attributes attributes;
        private final String authority;

        CallInfo(
                MethodDescriptor<ReqT, RespT> methodDescriptor,
                Attributes attributes,
                @Nullable String authority) {
            this.methodDescriptor = methodDescriptor;
            this.attributes = attributes;
            this.authority = authority;
        }

        @Override
        public MethodDescriptor<ReqT, RespT> getMethodDescriptor() {
            return methodDescriptor;
        }

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Nullable
        @Override
        public String getAuthority() {
            return authority;
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
    void serverBasicMetrics() {
        MetricsServerStreamTracers localServerStreamTracers =
                new MetricsServerStreamTracers(fakeClock.getStopwatchSupplier());
        ServerStreamTracer.Factory tracerFactory =
                localServerStreamTracers.getMetricsServerTracerFactory(meterRegistry);
        ServerStreamTracer tracer = tracerFactory.newServerStreamTracer(method.getFullMethodName(), new Metadata());
        tracer.serverCallStarted(
                new CallInfo<>(method, Attributes.EMPTY, null));

        assertThat(meterRegistry.get(SERVER_CALL_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .counter()
                .count()).isEqualTo(1);

        tracer.inboundWireSize(34);

        fakeClock.forwardTime(26, MILLISECONDS);

        tracer.outboundWireSize(1028);

        tracer.inboundWireSize(154);

        tracer.outboundWireSize(99);

        fakeClock.forwardTime(14, MILLISECONDS);

        tracer.streamClosed(Status.CANCELLED);

        HistogramSnapshot sentMessageSizeSnapShot = meterRegistry.get(SERVER_SENT_COMPRESSED_MESSAGE_SIZE)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(GRPC_STATUS_TAG_KEY, Status.Code.CANCELLED.toString())
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .summary()
                .takeSnapshot();
        HistogramSnapshot expectedSentMessageSizeHistogram = HistogramSnapshot.empty(1L, 1127L, 1127L);
        assertThat(sentMessageSizeSnapShot.count()).isEqualTo(expectedSentMessageSizeHistogram.count());
        assertThat(sentMessageSizeSnapShot.total()).isEqualTo(expectedSentMessageSizeHistogram.total());
        assertThat(sentMessageSizeSnapShot.histogramCounts()).contains(new CountAtBucket(2048.0, 1));

        HistogramSnapshot receivedMessageSizeSnapShot =
                meterRegistry.get(SERVER_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                        .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                        .tag(GRPC_STATUS_TAG_KEY, Status.Code.CANCELLED.toString())
                        .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                        .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                        .summary()
                        .takeSnapshot();
        HistogramSnapshot expectedReceivedMessageSizeHistogram = HistogramSnapshot.empty(1L, 188L, 188L);
        assertThat(receivedMessageSizeSnapShot.count()).isEqualTo(expectedReceivedMessageSizeHistogram.count());
        assertThat(receivedMessageSizeSnapShot.total()).isEqualTo(expectedReceivedMessageSizeHistogram.total());
        assertThat(receivedMessageSizeSnapShot.histogramCounts()).contains(new CountAtBucket(1024.0, 1));
        // TODO(dnvindhya) : Figure out a way to generate normal histogram instead of cumulative histogram
        // with fixed buckets
        /*
         * assertThat(receivedMessageSizeSnapShot.histogramCounts()).contains(new CountAtBucket(1024.0, 1), new
         * CountAtBucket(2048.0, 0));
         */

        HistogramSnapshot callDurationSnapshot = meterRegistry.get(SERVER_CALL_DURATION)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(GRPC_STATUS_TAG_KEY, Status.Code.CANCELLED.toString())
                .tag(INSTRUMENTATION_SOURCE_TAG_KEY, INSTRUMENTATION_SOURCE_TAG_VALUE)
                .tag(INSTRUMENTATION_VERSION_TAG_KEY, INSTRUMENTATION_VERSION_TAG_VALUE)
                .timer()
                .takeSnapshot();
        HistogramSnapshot expectedCallDurationHistogram = HistogramSnapshot.empty(1L, 40L, 40);
        assertThat(callDurationSnapshot.count()).isEqualTo(expectedCallDurationHistogram.count());
        assertThat(callDurationSnapshot.total(MILLISECONDS)).isEqualTo(expectedCallDurationHistogram.total());
        assertThat(callDurationSnapshot.histogramCounts()).contains(new CountAtBucket(4.0E7, 1));
    }
}

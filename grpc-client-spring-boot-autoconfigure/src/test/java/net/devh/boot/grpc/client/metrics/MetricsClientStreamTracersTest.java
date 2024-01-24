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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ClientStreamTracer;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.devh.boot.grpc.client.metrics.MetricsClientStreamTracers.CallAttemptsTracerFactory;

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
    @Mock
    private ClientCall.Listener<String> mockClientCallListener;
    @Mock
    private ServerCall.Listener<String> mockServerCallListener;

    @Captor
    private ArgumentCaptor<Status> statusCaptor;

    private FakeClock fakeClock;
    private MeterRegistry meterRegistry;

    private static ManagedChannel channel;

    @BeforeEach
    void setUp() {
        fakeClock = new FakeClock();
        meterRegistry = new SimpleMeterRegistry();
        Metrics.globalRegistry.add(meterRegistry);
        channel = InProcessChannelBuilder.forName("test").build();
    }

    @AfterEach
    void tearDown() {
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
        channel.shutdownNow();
    }

    @Test
    void testClientInterceptors() {
        MetricsClientStreamTracers module =
                new MetricsClientStreamTracers(fakeClock.getStopwatchSupplier());

        final AtomicReference<CallOptions> capturedCallOptions = new AtomicReference<>();
        ClientInterceptor callOptionsCaptureInterceptor = new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
                capturedCallOptions.set(callOptions);
                return next.newCall(method, callOptions);
            }
        };
        Channel interceptedChannel =
                ClientInterceptors.intercept(
                        channel, callOptionsCaptureInterceptor,
                        new MetricsClientInterceptor(meterRegistry, fakeClock.getStopwatchSupplier()));
        ClientCall<String, String> call;
        call = interceptedChannel.newCall(method, CALL_OPTIONS);


        assertThat(capturedCallOptions.get().getOption(CUSTOM_OPTION)).isEqualTo("customvalue");
        assertThat(capturedCallOptions.get().getStreamTracerFactories().size()).isEqualTo(1);
        assertThat(capturedCallOptions.get().getStreamTracerFactories()
                .get(0) instanceof MetricsClientStreamTracers.CallAttemptsTracerFactory).isTrue();
    }

    @Test
    public void clientBasicMetrics() {
        MetricsClientStreamTracers module =
                new MetricsClientStreamTracers(fakeClock.getStopwatchSupplier());
        MetricsClientMeters clientMeters = MetricsClientInstruments.newClientMetricsMeters(meterRegistry);
        MetricsClientStreamTracers.CallAttemptsTracerFactory callAttemptsTracerFactory =
                new CallAttemptsTracerFactory(module, method.getFullMethodName(), clientMeters);
        Metadata headers = new Metadata();
        ClientStreamTracer tracer =
                callAttemptsTracerFactory.newClientStreamTracer(STREAM_INFO, headers);

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .counter()
                .count()).isEqualTo(1);

        fakeClock.forwardTime(30, TimeUnit.MILLISECONDS);
        tracer.outboundWireSize(1028);

        fakeClock.forwardTime(100, TimeUnit.MILLISECONDS);
        tracer.outboundWireSize(99);

        fakeClock.forwardTime(24, TimeUnit.MILLISECONDS);
        tracer.inboundWireSize(111);

        tracer.streamClosed(Status.OK);
        callAttemptsTracerFactory.callEnded(Status.OK);

        assertThat(meterRegistry.get(CLIENT_ATTEMPT_STARTED)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .counter()
                .count()).isEqualTo(1);

        HistogramSnapshot attemptDurationSnapshot = meterRegistry.get(CLIENT_ATTEMPT_DURATION)
                .tag(GRPC_METHOD_TAG_KEY, FULL_METHOD_NAME)
                .tag(GRPC_STATUS_TAG_KEY, Status.Code.OK.toString())
                .timer()
                .takeSnapshot();
        HistogramSnapshot expectedCallDurationHistogram = HistogramSnapshot.empty(1L, 154L, 40);
        assertThat(attemptDurationSnapshot.count()).isEqualTo(expectedCallDurationHistogram.count());
        assertThat(attemptDurationSnapshot.total(MILLISECONDS)).isEqualTo(expectedCallDurationHistogram.total());
        assertThat(attemptDurationSnapshot.histogramCounts()).contains(new CountAtBucket(1.6E8, 1));
    }



}

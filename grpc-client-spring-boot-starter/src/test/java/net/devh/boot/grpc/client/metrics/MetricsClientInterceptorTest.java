/*
 * Copyright (c) 2016-2024 The gRPC-Spring Authors
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Tests for {@link MetricsClientInterceptor}.
 */
public class MetricsClientInterceptorTest {
    private static final CallOptions.Key<String> CUSTOM_OPTION =
            CallOptions.Key.createWithDefault("option1", "default");
    private static final CallOptions CALL_OPTIONS =
            CallOptions.DEFAULT.withOption(CUSTOM_OPTION, "customvalue");
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

    private FakeClock fakeClock;
    private MeterRegistry meterRegistry;
    private ManagedChannel channel;

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

}

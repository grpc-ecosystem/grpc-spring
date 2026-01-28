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

package net.devh.boot.grpc.test.advice;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.micrometer.core.instrument.binder.grpc.GrpcObservationDocumentation.LowCardinalityKeyNames.STATUS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerMetricAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerMicrometerTraceAutoConfiguration;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;



@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringJUnitConfig(classes = {
        InProcessConfiguration.class,
        BaseAutoConfiguration.class,
        GrpcAdviceWithMetricsTest.TestConfig.class
})
@ImportAutoConfiguration(classes = {GrpcAdviceAutoConfiguration.class,
        GrpcServerMetricAutoConfiguration.class,
        GrpcServerMicrometerTraceAutoConfiguration.class})
@AutoConfigureObservability
@DirtiesContext
class GrpcAdviceWithMetricsTest {

    @GrpcClient("test")
    protected TestServiceGrpc.TestServiceBlockingStub blockingStub;

    @Autowired
    private MeterRegistry meterRegistry;

    public static Stream<Arguments> metricsFlavourProvider() {
        return Stream.of(
                Arguments.of("grpc.server.processing.duration", "statusCode"),
                Arguments.of("grpc.server", STATUS_CODE.asString()));
    }

    @BeforeEach
    public void setUp() {
        meterRegistry.clear();
    }

    @ParameterizedTest
    @MethodSource("metricsFlavourProvider")
    void shouldRegisterMetricsStatusCodeWhenUsingGrpcAdvice(String metricName, String statusCodeTagName) {
        var exception = assertThrows(StatusRuntimeException.class, () -> {
            blockingStub.error(Empty.getDefaultInstance());
        });
        assertEquals(INVALID_ARGUMENT, exception.getStatus());

        var meter = waitForMeter(metricName, statusCodeTagName);
        assertEquals(1L, meter.count());
    }

    @ParameterizedTest
    @MethodSource("metricsFlavourProvider")
    void shouldRegisterMetricsStatusCodeForServerStreamingWithResponseObserverOnError(String metricName,
            String statusCodeTagName) {
        var exception = assertThrows(StatusRuntimeException.class, () -> {
            var iterator = blockingStub.secureSupply(Empty.getDefaultInstance());
            while (iterator.hasNext()) {
                iterator.next();
            }
        });
        assertEquals(INVALID_ARGUMENT, exception.getStatus());

        var meter = waitForMeter(metricName, statusCodeTagName);
        assertEquals(1L, meter.count());
    }

    private Timer waitForMeter(String metricName, String statusCodeTagName) {
        // Poll for up to 200ms for metrics to be recorded
        // ObservationGrpcServerInterceptor may record metrics asynchronously
        for (int i = 0; i < 20; i++) {
            var meter = meterRegistry.find(metricName)
                    .tags(statusCodeTagName, Status.Code.INVALID_ARGUMENT.name()).timer();
            if (meter != null && meter.count() > 0) {
                return meter;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for meter", e);
            }
        }
        return Optional.ofNullable(meterRegistry.find(metricName)
                .tags(statusCodeTagName, Status.Code.INVALID_ARGUMENT.name()).timer())
                .orElseGet(() -> fail("Expected meter with statusCode to be registered within 200ms"));
    }


    @TestConfiguration
    static class TestConfig {

        @GrpcAdvice
        public static class ExceptionHandler {
            @GrpcExceptionHandler
            public Status handleAnyException(Exception e) {
                return INVALID_ARGUMENT.withCause(e);
            }
        }

        @GrpcService
        public static class TestGrpcAdviceService extends TestServiceGrpc.TestServiceImplBase {
            @Override
            public void error(Empty request, StreamObserver<Empty> responseObserver) {
                throw new RuntimeException("a simulated error");
            }

            @Override
            public void secureSupply(Empty request,
                    StreamObserver<net.devh.boot.grpc.test.proto.SomeType> responseObserver) {
                // Server streaming - use responseObserver.onError()
                responseObserver.onError(new RuntimeException("server streaming error via responseObserver.onError"));
            }
        }
    }
}

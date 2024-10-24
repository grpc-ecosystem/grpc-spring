
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

        var meter = Optional.ofNullable(meterRegistry.find(metricName)
                .tags(statusCodeTagName, Status.Code.INVALID_ARGUMENT.name()).timer())
                .orElseGet(() -> fail("expected meter with statusCode to be registered"));

        assertEquals(1L, meter.count());
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
        }
    }
}

/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.test.metric;

import static io.grpc.Status.Code.CANCELLED;
import static io.grpc.Status.Code.INTERNAL;
import static io.grpc.Status.Code.UNIMPLEMENTED;
import static io.grpc.Status.Code.UNKNOWN;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_PROCESSING_DURATION;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_REQUESTS_SENT;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_RESPONSES_RECEIVED;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_PROCESSING_DURATION;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_REQUESTS_RECEIVED;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_RESPONSES_SENT;
import static net.devh.boot.grpc.common.metric.MetricConstants.TAG_METHOD_NAME;
import static net.devh.boot.grpc.common.metric.MetricConstants.TAG_STATUS_CODE;
import static net.devh.boot.grpc.test.config.AwaitableServerClientCallConfiguration.awaitNextServerAndClientCallCloses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientMetricAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.common.metric.MetricConstants;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerMetricAutoConfiguration;
import net.devh.boot.grpc.test.config.AwaitableServerClientCallConfiguration;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.MetricConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

/**
 * A full test with Spring for both the server side and the client side interceptors.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest(properties = {
        "grpc.client.GLOBAL.address=localhost:9090",
        "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
})
@SpringJUnitConfig(classes = {
        MetricConfiguration.class,
        ServiceConfiguration.class,
        BaseAutoConfiguration.class,
        AwaitableServerClientCallConfiguration.class,
})
@ImportAutoConfiguration({
        GrpcClientMetricAutoConfiguration.class,
        GrpcServerMetricAutoConfiguration.class,
})
@DirtiesContext
class MetricCollectingInterceptorTest {

    private static final Empty EMPTY = Empty.getDefaultInstance();

    @Autowired
    private MeterRegistry meterRegistry;

    @GrpcClient("test")
    private TestServiceBlockingStub testService;

    @GrpcClient("test")
    private TestServiceStub testStreamService;

    /**
     * Test successful call.
     */
    @Test
    @DirtiesContext
    void testMetricsSuccessfulCall() {
        log.info("--- Starting tests with successful call ---");
        CountDownLatch counter = awaitNextServerAndClientCallCloses(1);

        // Invoke 1
        assertEquals("1.2.3", this.testService.normal(EMPTY).getVersion());

        assertTimeoutPreemptively(Duration.ofSeconds(1), (Executable) counter::await);

        // Test-Client 1
        final Counter requestSentCounter =
                this.meterRegistry.find(METRIC_NAME_CLIENT_REQUESTS_SENT).counter();
        assertNotNull(requestSentCounter);
        assertEquals(1, requestSentCounter.count());

        final Counter responseReceivedCounter =
                this.meterRegistry.find(METRIC_NAME_CLIENT_RESPONSES_RECEIVED).counter();
        assertNotNull(responseReceivedCounter);
        assertEquals(1, responseReceivedCounter.count());

        final Timer clientTimer =
                this.meterRegistry.find(METRIC_NAME_CLIENT_PROCESSING_DURATION).timer();
        assertNotNull(clientTimer);
        assertEquals(1, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 1);

        // Test-Server 1
        for (final Meter meter : this.meterRegistry.find(METRIC_NAME_SERVER_REQUESTS_RECEIVED).counters()) {
            log.debug("Found meter: {}", meter.getId());
        }
        final Counter requestsReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(TAG_METHOD_NAME, "normal")
                .counter();
        assertNotNull(requestsReceivedCounter);
        assertEquals(1, requestsReceivedCounter.count());

        final Counter responsesSentCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_RESPONSES_SENT)
                .tag(TAG_METHOD_NAME, "normal")
                .counter();
        assertNotNull(responsesSentCounter);
        assertEquals(1, responsesSentCounter.count());

        final Timer serverTimer = this.meterRegistry
                .find(METRIC_NAME_SERVER_PROCESSING_DURATION)
                .tag(TAG_METHOD_NAME, "normal")
                .timer();
        assertNotNull(serverTimer);
        assertEquals(1, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 1);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));

        // --------------------------------------------------------------------

        counter = awaitNextServerAndClientCallCloses(1);

        // Invoke 2
        assertEquals("1.2.3", this.testService.normal(EMPTY).getVersion());

        assertTimeoutPreemptively(Duration.ofSeconds(1), (Executable) counter::await);

        // Test-Client 2
        assertEquals(2, requestSentCounter.count());
        assertEquals(2, responseReceivedCounter.count());
        assertEquals(2, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 1);

        // Test-Server 2
        assertEquals(2, requestsReceivedCounter.count());
        assertEquals(2, responsesSentCounter.count());
        assertEquals(2, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 1);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));
        log.info("--- Test completed ---");
    }

    /**
     * Test early cancelled call.
     */
    @Test
    @DirtiesContext
    void testMetricsEarlyCancelledCall() {
        log.info("--- Starting tests with early cancelled call ---");
        final AtomicReference<Throwable> exception = new AtomicReference<>();
        final CountDownLatch counter = awaitNextServerAndClientCallCloses(1);

        // Invoke
        final ClientCallStreamObserver<SomeType> observer =
                (ClientCallStreamObserver<SomeType>) this.testStreamService.echo(new StreamObserver<SomeType>() {

                    @Override
                    public void onNext(final SomeType value) {
                        try {
                            fail("Should never be here");
                        } catch (final RuntimeException t) {
                            setError(t);
                            throw t;
                        }
                    }

                    @Override
                    public void onError(final Throwable t) {
                        setError(t);
                    }

                    @Override
                    public void onCompleted() {
                        try {
                            fail("Should never be here");
                        } catch (final RuntimeException t) {
                            setError(t);
                            throw t;
                        }
                    }

                    private synchronized void setError(final Throwable t) {
                        final Throwable previous = exception.get();
                        if (previous == null) {
                            exception.set(t);
                        } else {
                            previous.addSuppressed(t);
                        }
                    }

                });

        assertDoesNotThrow(() -> counter.await(1, TimeUnit.SECONDS));

        observer.cancel("Cancelled", null);
        assertTimeoutPreemptively(Duration.ofSeconds(3), (Executable) counter::await);
        assertThat(exception.get())
                .isNotNull()
                .isInstanceOfSatisfying(StatusRuntimeException.class,
                        t -> assertEquals(CANCELLED, t.getStatus().getCode()));

        // Test-Client
        final Counter requestSentCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_REQUESTS_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(requestSentCounter);
        assertEquals(0, requestSentCounter.count());

        final Counter responseReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_RESPONSES_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(responseReceivedCounter);
        assertEquals(0, responseReceivedCounter.count());

        final Timer clientTimer = this.meterRegistry
                .find(METRIC_NAME_CLIENT_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .tag(TAG_STATUS_CODE, CANCELLED.name())
                .timer();
        assertNotNull(clientTimer);
        assertEquals(1, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 3);

        // Test-Server
        final Counter requestsReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(requestsReceivedCounter);
        assertEquals(0, requestsReceivedCounter.count());

        final Counter responsesSentCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_RESPONSES_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(responsesSentCounter);
        assertEquals(0, responsesSentCounter.count());

        final Timer serverTimer = this.meterRegistry
                .find(METRIC_NAME_SERVER_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .tag(TAG_STATUS_CODE, CANCELLED.name())
                .timer();
        assertNotNull(serverTimer);
        assertEquals(1, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 3);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));
        log.info("--- Test completed ---");
    }

    /**
     * Test cancelled call.
     */
    @Test
    @DirtiesContext
    void testMetricsCancelledCall() {
        log.info("--- Starting tests with cancelled call ---");
        final AtomicReference<Throwable> exception = new AtomicReference<>();
        final CountDownLatch counter = awaitNextServerAndClientCallCloses(1);

        // Invoke
        final ClientCallStreamObserver<SomeType> observer =
                (ClientCallStreamObserver<SomeType>) this.testStreamService.echo(new StreamObserver<SomeType>() {

                    @Override
                    public void onNext(final SomeType value) {}

                    @Override
                    public void onError(final Throwable t) {
                        setError(t);
                    }

                    @Override
                    public void onCompleted() {
                        try {
                            fail("Should never be here");
                        } catch (final RuntimeException t) {
                            setError(t);
                            throw t;
                        }
                    }

                    private synchronized void setError(final Throwable t) {
                        final Throwable previous = exception.get();
                        if (previous == null) {
                            exception.set(t);
                        } else {
                            previous.addSuppressed(t);
                        }
                    }

                });

        observer.onNext(SomeType.getDefaultInstance());
        assertDoesNotThrow(() -> counter.await(1, TimeUnit.SECONDS));

        observer.cancel("Cancelled", null);
        assertTimeoutPreemptively(Duration.ofSeconds(3), (Executable) counter::await);
        assertThat(exception.get())
                .isNotNull()
                .isInstanceOfSatisfying(StatusRuntimeException.class,
                        t -> assertEquals(CANCELLED, t.getStatus().getCode()));

        // Test-Client
        final Counter requestSentCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_REQUESTS_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(requestSentCounter);
        assertEquals(1, requestSentCounter.count());

        final Counter responseReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_RESPONSES_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(responseReceivedCounter);
        assertEquals(1, responseReceivedCounter.count());

        final Timer clientTimer = this.meterRegistry
                .find(METRIC_NAME_CLIENT_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .tag(TAG_STATUS_CODE, CANCELLED.name())
                .timer();
        assertNotNull(clientTimer);
        assertEquals(1, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 3);

        // Test-Server
        final Counter requestsReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(requestsReceivedCounter);
        assertEquals(1, requestsReceivedCounter.count());

        final Counter responsesSentCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_RESPONSES_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .counter();
        assertNotNull(responsesSentCounter);
        assertEquals(1, responsesSentCounter.count());

        final Timer serverTimer = this.meterRegistry
                .find(METRIC_NAME_SERVER_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "echo")
                .tag(TAG_STATUS_CODE, CANCELLED.name())
                .timer();
        assertNotNull(serverTimer);
        assertEquals(1, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 3);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));
        log.info("--- Test completed ---");
    }

    /**
     * Test unimplemented call.
     */
    @Test
    @DirtiesContext
    void testMetricsUniplementedCall() {
        log.info("--- Starting tests with unimplemented call ---");

        final CountDownLatch counter = awaitNextServerAndClientCallCloses(1);

        // Invoke
        assertThrows(StatusRuntimeException.class,
                () -> this.testService.unimplemented(EMPTY));

        assertTimeoutPreemptively(Duration.ofSeconds(1), (Executable) counter::await);

        // Test-Client
        final Counter requestSentCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_REQUESTS_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "unimplemented")
                .counter();
        assertNotNull(requestSentCounter);
        assertEquals(1, requestSentCounter.count());

        final Counter responseReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_RESPONSES_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "unimplemented")
                .counter();
        assertNotNull(responseReceivedCounter);
        assertEquals(0, responseReceivedCounter.count());

        final Timer clientTimer = this.meterRegistry
                .find(METRIC_NAME_CLIENT_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "unimplemented")
                .tag(TAG_STATUS_CODE, UNIMPLEMENTED.name())
                .timer();
        assertNotNull(clientTimer);
        assertEquals(1, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 1);

        // Test-Server
        final Counter requestsReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "unimplemented")
                .counter();
        assertNotNull(requestsReceivedCounter);
        assertEquals(1, requestsReceivedCounter.count());

        final Counter responsesSentCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_RESPONSES_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "unimplemented")
                .counter();
        assertNotNull(responsesSentCounter);
        assertEquals(0, responsesSentCounter.count());

        final Timer serverTimer = this.meterRegistry
                .find(METRIC_NAME_SERVER_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "unimplemented")
                .tag(TAG_STATUS_CODE, UNIMPLEMENTED.name())
                .timer();
        assertNotNull(serverTimer);
        assertEquals(1, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 1);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));
        log.info("--- Test completed ---");
    }

    /**
     * Test failed call.
     */
    @Test
    @DirtiesContext
    void testMetricsFailedCall() {
        log.info("--- Starting tests with failing call ---");

        final CountDownLatch counter = awaitNextServerAndClientCallCloses(1);

        // Invoke
        assertThrows(StatusRuntimeException.class, () -> this.testService.secure(EMPTY));

        assertTimeoutPreemptively(Duration.ofSeconds(1), (Executable) counter::await);

        // Test-Client
        final Counter requestSentCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_REQUESTS_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "secure")
                .counter();
        assertNotNull(requestSentCounter);
        assertEquals(1, requestSentCounter.count());

        final Counter responseReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_RESPONSES_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "secure")
                .counter();
        assertNotNull(responseReceivedCounter);
        assertEquals(0, responseReceivedCounter.count());

        final Timer clientTimer = this.meterRegistry
                .find(METRIC_NAME_CLIENT_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "secure")
                .tag(TAG_STATUS_CODE, UNKNOWN.name())
                .timer();
        assertNotNull(clientTimer);
        assertEquals(1, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 1);

        // Test-Server
        final Counter requestsReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "secure")
                .counter();
        assertNotNull(requestsReceivedCounter);
        assertEquals(1, requestsReceivedCounter.count());

        final Counter responsesSentCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_RESPONSES_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "secure")
                .counter();
        assertNotNull(responsesSentCounter);
        assertEquals(0, responsesSentCounter.count());

        final Timer serverTimer = this.meterRegistry
                .find(METRIC_NAME_SERVER_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "secure")
                .tag(TAG_STATUS_CODE, UNKNOWN.name())
                .timer();
        assertNotNull(serverTimer);
        assertEquals(1, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 1);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));
        log.info("--- Test completed ---");
    }

    /**
     * Test error call.
     */
    @Test
    @DirtiesContext
    void testMetricsErrorCall() {
        log.info("--- Starting tests with error status call ---");

        final CountDownLatch counter = awaitNextServerAndClientCallCloses(1);

        // Invoke
        assertThrows(StatusRuntimeException.class, () -> this.testService.error(EMPTY));

        assertTimeoutPreemptively(Duration.ofSeconds(1), (Executable) counter::await);

        // Test-Client
        final Counter requestSentCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_REQUESTS_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "error")
                .counter();
        assertNotNull(requestSentCounter);
        assertEquals(1, requestSentCounter.count());

        final Counter responseReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_CLIENT_RESPONSES_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "error")
                .counter();
        assertNotNull(responseReceivedCounter);
        assertEquals(0, responseReceivedCounter.count());

        final Timer clientTimer = this.meterRegistry
                .find(METRIC_NAME_CLIENT_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "error")
                .tag(TAG_STATUS_CODE, INTERNAL.name())
                .timer();
        assertNotNull(clientTimer);
        assertEquals(1, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 1);

        // Test-Server
        final Counter requestsReceivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "error")
                .counter();
        assertNotNull(requestsReceivedCounter);
        assertEquals(1, requestsReceivedCounter.count());

        final Counter responsesSentCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_RESPONSES_SENT)
                .tag(MetricConstants.TAG_METHOD_NAME, "error")
                .counter();
        assertNotNull(responsesSentCounter);
        assertEquals(0, responsesSentCounter.count());

        final Timer serverTimer = this.meterRegistry
                .find(METRIC_NAME_SERVER_PROCESSING_DURATION)
                .tag(MetricConstants.TAG_METHOD_NAME, "error")
                .tag(TAG_STATUS_CODE, INTERNAL.name())
                .timer();
        assertNotNull(serverTimer);
        assertEquals(1, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 1);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));
        log.info("--- Test completed ---");
    }

}

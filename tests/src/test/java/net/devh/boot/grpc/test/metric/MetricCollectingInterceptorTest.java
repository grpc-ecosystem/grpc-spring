/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

import static io.grpc.Status.Code.UNIMPLEMENTED;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_PROCESSING_DURATION;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_REQUESTS_SENT;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_RESPONSES_RECEIVED;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_PROCESSING_DURATION;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_REQUESTS_RECEIVED;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_SERVER_RESPONSES_SENT;
import static net.devh.boot.grpc.common.metric.MetricConstants.TAG_METHOD_NAME;
import static net.devh.boot.grpc.common.metric.MetricConstants.TAG_STATUS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.StatusRuntimeException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientMetricAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.common.metric.MetricConstants;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerMetricAutoConfiguration;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.MetricConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;

/**
 * A full test with Spring for both the server side and the client side interceptors.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest(properties = "grpc.client.test.negotiationType=PLAINTEXT")
@SpringJUnitConfig(classes = {MetricConfiguration.class, ServiceConfiguration.class, BaseAutoConfiguration.class})
@ImportAutoConfiguration({GrpcClientMetricAutoConfiguration.class, GrpcServerMetricAutoConfiguration.class})
@DirtiesContext
public class MetricCollectingInterceptorTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @GrpcClient("test")
    private TestServiceBlockingStub testService;

    /**
     * Test successful call.
     */
    @Test
    @DirtiesContext
    public void testMetricsSuccessfulCall() {
        log.info("--- Starting tests with successful call ---");
        // Invoke 1
        assertEquals("1.2.3", this.testService.normal(Empty.getDefaultInstance()).getVersion());

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
        final Counter requestsReveivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(TAG_METHOD_NAME, "normal")
                .counter();
        assertNotNull(requestsReveivedCounter);
        assertEquals(1, requestsReveivedCounter.count());

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

        // Invoke 2
        assertEquals("1.2.3", this.testService.normal(Empty.getDefaultInstance()).getVersion());

        // Test-Client 2
        assertEquals(2, requestSentCounter.count());
        assertEquals(2, responseReceivedCounter.count());
        assertEquals(2, clientTimer.count());
        assertTrue(clientTimer.max(TimeUnit.SECONDS) < 1);

        // Test-Server 2
        assertEquals(2, requestsReveivedCounter.count());
        assertEquals(2, responsesSentCounter.count());
        assertEquals(2, serverTimer.count());
        assertTrue(serverTimer.max(TimeUnit.SECONDS) < 1);

        // Client has network overhead so it has to be slower
        assertTrue(serverTimer.max(TimeUnit.SECONDS) <= clientTimer.max(TimeUnit.SECONDS));
        log.info("--- Test completed ---");
    }

    /**
     * Test failing call.
     */
    @Test
    @DirtiesContext
    public void testMetricsFailingCall() {
        log.info("--- Starting tests with failing call ---");
        // Invoke
        assertThrows(StatusRuntimeException.class,
                () -> this.testService.unimplemented(Empty.getDefaultInstance()).getVersion());

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
        final Counter requestsReveivedCounter = this.meterRegistry
                .find(METRIC_NAME_SERVER_REQUESTS_RECEIVED)
                .tag(MetricConstants.TAG_METHOD_NAME, "unimplemented")
                .counter();
        assertNotNull(requestsReveivedCounter);
        assertEquals(1, requestsReveivedCounter.count());

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

}

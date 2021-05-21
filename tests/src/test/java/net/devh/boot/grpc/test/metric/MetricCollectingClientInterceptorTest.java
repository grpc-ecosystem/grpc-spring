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

import static io.grpc.Status.Code.OK;
import static io.grpc.Status.Code.UNKNOWN;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_PROCESSING_DURATION;
import static net.devh.boot.grpc.common.metric.MetricConstants.METRIC_NAME_CLIENT_REQUESTS_SENT;
import static net.devh.boot.grpc.test.server.TestServiceImpl.METHOD_COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.metric.MetricCollectingClientInterceptor;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

@Slf4j
class MetricCollectingClientInterceptorTest {

    @Test
    void testClientPreRegistration() {
        log.info("--- Starting tests with client pre-registration ---");
        final MeterRegistry meterRegistry = new SimpleMeterRegistry();
        assertEquals(0, meterRegistry.getMeters().size());
        final MetricCollectingClientInterceptor mcci = new MetricCollectingClientInterceptor(meterRegistry);
        mcci.preregisterService(TestServiceGrpc.getServiceDescriptor());

        MetricTestHelper.logMeters(meterRegistry.getMeters());
        assertEquals(METHOD_COUNT * 3, meterRegistry.getMeters().size());
        log.info("--- Test completed ---");
    }

    @Test
    void testClientCustomization() {
        log.info("--- Starting tests with client customization ---");
        final MeterRegistry meterRegistry = new SimpleMeterRegistry();
        assertEquals(0, meterRegistry.getMeters().size());
        final MetricCollectingClientInterceptor mcci = new MetricCollectingClientInterceptor(meterRegistry,
                counter -> counter.tag("type", "counter"),
                timer -> timer.tag("type", "timer").publishPercentiles(0.5, 0.9, 0.99),
                OK, UNKNOWN);
        mcci.preregisterService(TestServiceGrpc.getServiceDescriptor());

        MetricTestHelper.logMeters(meterRegistry.getMeters());
        assertEquals(METHOD_COUNT * 10, meterRegistry.getMeters().size());

        final Counter counter = meterRegistry.find(METRIC_NAME_CLIENT_REQUESTS_SENT).counter();
        assertNotNull(counter);
        assertEquals("counter", counter.getId().getTag("type"));

        final Timer timer = meterRegistry.find(METRIC_NAME_CLIENT_PROCESSING_DURATION).timer();
        assertNotNull(timer);
        assertEquals("timer", timer.getId().getTag("type"));
        log.info("--- Test completed ---");
    }

}

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

import static net.devh.boot.grpc.test.server.TestServiceImpl.METHOD_COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.health.v1.HealthGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.test.config.ServiceConfiguration;

/**
 * A test to verify that the server auto configuration works.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = ServiceConfiguration.class)
@EnableAutoConfiguration
@DirtiesContext
class MetricFullAutoConfigurationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    private static final int HEALTH_SERVICE_METHOD_COUNT =
            HealthGrpc.getServiceDescriptor().getMethods().size();
    private static final int REFLECTION_SERVICE_METHOD_COUNT =
            ServerReflectionGrpc.getServiceDescriptor().getMethods().size();
    private static final int TOTAL_METHOD_COUNT =
            HEALTH_SERVICE_METHOD_COUNT + REFLECTION_SERVICE_METHOD_COUNT + METHOD_COUNT;

    @Test
    @DirtiesContext
    void testAutoDiscovery() {
        log.info("--- Starting tests with full auto discovery ---");
        MetricTestHelper.logMeters(this.meterRegistry.getMeters());
        assertEquals(TOTAL_METHOD_COUNT * 2, this.meterRegistry.getMeters().stream()
                .filter(Counter.class::isInstance)
                .filter(m -> m.getId().getName().startsWith("grpc.")) // Only count grpc metrics
                .count());
        assertEquals(TOTAL_METHOD_COUNT, this.meterRegistry.getMeters().stream()
                .filter(Timer.class::isInstance)
                .filter(m -> m.getId().getName().startsWith("grpc.")) // Only count grpc metrics
                .count());
        log.info("--- Test completed ---");
    }

}

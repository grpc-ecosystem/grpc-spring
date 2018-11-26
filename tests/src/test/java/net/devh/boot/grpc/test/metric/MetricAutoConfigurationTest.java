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

import static net.devh.boot.grpc.test.server.TestServiceImpl.METHOD_COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerMetricAutoConfiguration;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.MetricConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;

/**
 * A test to verify that the server auto configuration works.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = {MetricConfiguration.class, ServiceConfiguration.class, BaseAutoConfiguration.class})
@ImportAutoConfiguration(GrpcServerMetricAutoConfiguration.class)
@DirtiesContext
public class MetricAutoConfigurationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DirtiesContext
    public void testAutoDiscovery() {
        log.info("--- Starting tests with auto discovery ---");
        for (final Meter meter : meterRegistry.getMeters()) {
            log.debug("Found meter: {}", meter.getId());
        }
        assertEquals(METHOD_COUNT * 2,
                this.meterRegistry.getMeters().stream().filter(Counter.class::isInstance).count());
        assertEquals(METHOD_COUNT, this.meterRegistry.getMeters().stream().filter(Timer.class::isInstance).count());
        log.info("--- Test completed ---");
    }

}

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

package net.devh.boot.grpc.test.advice;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;

/**
 * A test to verify that the grpc exception advice is not automatically picked up, if no {@link GrpcAdvice @GrpcAdvice}
 * is present.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = BaseAutoConfiguration.class)
@ImportAutoConfiguration(GrpcAdviceAutoConfiguration.class)
@DirtiesContext
class AdviceNotPresentAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DirtiesContext
    void testGrpcAdviceNotPresent() {
        log.info("--- Starting tests with no present advice - auto discovery ---");

        Map<String, Object> actualAdviceBeans = applicationContext.getBeansWithAnnotation(GrpcAdvice.class);

        Assertions.assertThat(actualAdviceBeans).isEmpty();
    }

    @Test
    @DirtiesContext
    void testGrpcExceptionHandlerNotPresent() {
        log.info("--- Starting tests with no present advice - auto discovery ---");

        Map<String, Object> actualExceptionHandler =
                applicationContext.getBeansWithAnnotation(GrpcExceptionHandler.class);

        Assertions.assertThat(actualExceptionHandler).isEmpty();
    }


    @BeforeAll
    public static void beforeAll() {
        log.info("--- Starting tests for no present advice ---");
    }

    @AfterAll
    public static void afterAll() {
        log.info("--- Ending tests with for no present advice ---");
    }

}

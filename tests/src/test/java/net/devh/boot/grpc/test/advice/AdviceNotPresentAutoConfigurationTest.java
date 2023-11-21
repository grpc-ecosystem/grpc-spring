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

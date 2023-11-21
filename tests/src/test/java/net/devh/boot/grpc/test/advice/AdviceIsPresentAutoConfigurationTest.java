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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdviceDiscoverer;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceForInheritedExceptions;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithMetadata;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithOutMetadata;

/**
 * A test to verify that the grpc exception advice auto configuration works.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = {GrpcAdviceConfig.class, BaseAutoConfiguration.class})
@ImportAutoConfiguration(GrpcAdviceAutoConfiguration.class)
@DirtiesContext
class AdviceIsPresentAutoConfigurationTest {

    private static final int ADVICE_CLASSES = 3;
    private static final int ADVICE_METHODS = 7;


    @Autowired
    private GrpcAdviceDiscoverer grpcAdviceDiscoverer;

    @Autowired
    private TestAdviceWithOutMetadata testAdviceWithOutMetadata;
    @Autowired
    private TestAdviceWithMetadata testAdviceWithMetadata;
    @Autowired
    private TestAdviceForInheritedExceptions testAdviceForInheritedExceptions;


    @Test
    @DirtiesContext
    void testAdviceIsPresentWithExceptionMapping() {
        log.info("--- Starting tests with advice auto discovery ---");

        Map<String, Object> expectedAdviceBeans = new HashMap<>();
        expectedAdviceBeans.put("grpcAdviceWithBean", testAdviceWithOutMetadata);
        expectedAdviceBeans.put(TestAdviceWithMetadata.class.getName(), testAdviceWithMetadata);
        expectedAdviceBeans.put(TestAdviceForInheritedExceptions.class.getName(), testAdviceForInheritedExceptions);
        Set<Method> expectedAdviceMethods = expectedMethods();

        Map<String, Object> actualAdviceBeans = grpcAdviceDiscoverer.getAnnotatedBeans();
        Set<Method> actualAdviceMethods = grpcAdviceDiscoverer.getAnnotatedMethods();

        assertThat(actualAdviceBeans)
                .hasSize(ADVICE_CLASSES)
                .containsExactlyInAnyOrderEntriesOf(expectedAdviceBeans);
        assertThat(actualAdviceMethods)
                .hasSize(ADVICE_METHODS)
                .containsExactlyInAnyOrderElementsOf(expectedAdviceMethods);
    }

    // ###################
    // ### H E L P E R ###
    // ###################

    private Set<Method> expectedMethods() {
        new HashSet<>();
        Set<Method> methodsWithMetadata =
                Arrays.stream(testAdviceWithMetadata.getClass().getDeclaredMethods()).collect(Collectors.toSet());
        Set<Method> methodsWithOutMetadata =
                Arrays.stream(testAdviceWithOutMetadata.getClass().getDeclaredMethods()).collect(Collectors.toSet());
        Set<Method> methodsForInheritedExceptions =
                Arrays.stream(testAdviceForInheritedExceptions.getClass().getDeclaredMethods())
                        .collect(Collectors.toSet());

        return Stream.of(methodsWithMetadata, methodsWithOutMetadata, methodsForInheritedExceptions)
                .flatMap(Collection::stream)
                .filter(method -> method.isAnnotationPresent(GrpcExceptionHandler.class))
                .collect(Collectors.toSet());
    }

    @BeforeAll
    public static void beforeAll() {
        log.info("--- Starting tests with successful advice pickup ---");
    }

    @AfterAll
    public static void afterAll() {
        log.info("--- Ending tests with successful advice pickup ---");
    }

}

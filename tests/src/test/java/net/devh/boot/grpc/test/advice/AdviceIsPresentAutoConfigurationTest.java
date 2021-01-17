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

    private static final int ADVICE_CLASSES = 2;
    private static final int ADVICE_METHODS = 5;


    @Autowired
    private GrpcAdviceDiscoverer grpcAdviceDiscoverer;

    @Autowired
    private TestAdviceWithOutMetadata testAdviceWithOutMetadata;
    @Autowired
    private TestAdviceWithMetadata testAdviceWithMetadata;


    @Test
    @DirtiesContext
    void testAdviceIsPresentWithExceptionMapping() {
        log.info("--- Starting tests with advice auto discovery ---");

        Map<String, Object> expectedAdviceBeans = new HashMap<>();
        expectedAdviceBeans.put("grpcAdviceWithBean", testAdviceWithOutMetadata);
        expectedAdviceBeans.put(TestAdviceWithMetadata.class.getName(), testAdviceWithMetadata);
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

        return Stream.of(methodsWithMetadata, methodsWithOutMetadata)
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

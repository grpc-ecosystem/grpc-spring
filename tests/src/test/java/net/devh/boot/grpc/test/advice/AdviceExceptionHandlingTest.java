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

import java.security.AccessControlException;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionHandler;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionListener;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithMetadata.FirstLevelException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithMetadata.MyRootRuntimeException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithMetadata.SecondLevelException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithMetadata.StatusMappingException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestGrpcAdviceService;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.util.LoggerTestUtil;

/**
 * A test checking that the server and client can start and connect to each other with minimal config and a exception
 * advice is applied.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = {
        InProcessConfiguration.class,
        GrpcAdviceConfig.class,
        BaseAutoConfiguration.class
})
@ImportAutoConfiguration(GrpcAdviceAutoConfiguration.class)
@DirtiesContext
class AdviceExceptionHandlingTest extends AbstractSimpleServerClientTest {

    private ListAppender<ILoggingEvent> loggingEventListAppender;

    @Autowired
    private TestGrpcAdviceService testGrpcAdviceService;

    @BeforeEach
    void setup() {
        loggingEventListAppender = LoggerTestUtil.getListAppenderForClasses(
                GrpcAdviceExceptionListener.class,
                GrpcAdviceExceptionHandler.class);
    }

    @Test
    @DirtiesContext
    void testThrownIllegalArgumentException_IsMappedAsStatus() {

        IllegalArgumentException exceptionToMap = new IllegalArgumentException("Trigger Advice");
        testGrpcAdviceService.setExceptionToSimulate(exceptionToMap);
        Status expectedStatus = Status.INVALID_ARGUMENT.withDescription(exceptionToMap.getMessage());
        Metadata metadata = new Metadata();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @Test
    @DirtiesContext
    void testThrownAccessControlException_IsMappedAsThrowable() {

        AccessControlException exceptionToMap = new AccessControlException("Trigger Advice");
        testGrpcAdviceService.setExceptionToSimulate(exceptionToMap);
        Status expectedStatus = Status.UNKNOWN;
        Metadata metadata = new Metadata();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @Test
    @DirtiesContext
    void testThrownClassCastException_IsMappedAsStatusRuntimeExceptionAndWithMetadata() {

        ClassCastException exceptionToMap = new ClassCastException("Casting with classes failed.");
        testGrpcAdviceService.setExceptionToSimulate(exceptionToMap);
        Status expectedStatus = Status.FAILED_PRECONDITION.withDescription(exceptionToMap.getMessage());
        Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @Test
    @DirtiesContext
    void testThrownMyRootRuntimeException_IsNotMappedAndResultsInInvocationException() {

        AccountExpiredException exceptionToMap =
                new AccountExpiredException("Trigger Advice"); // not mapped in GrpcAdviceConfig
        testGrpcAdviceService.setExceptionToSimulate(exceptionToMap);
        Status expectedStatus =
                Status.INTERNAL.withDescription("There was a server error trying to handle an exception");
        Metadata metadata = new Metadata();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        assertThat(loggingEventListAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Exception caught during gRPC execution: ", Level.DEBUG))
                .contains(Tuple.tuple(
                        "Exception thrown during invocation of annotated @GrpcExceptionHandler method: ",
                        Level.ERROR));
    }

    @Test
    @DirtiesContext
    void testThrownFirstLevelException_IsMappedAsStatusExceptionWithMetadata() {

        FirstLevelException exceptionToMap = new FirstLevelException("Trigger Advice");
        testGrpcAdviceService.setExceptionToSimulate(exceptionToMap);
        Status expectedStatus = Status.NOT_FOUND.withDescription(exceptionToMap.getMessage());
        Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @Test
    @DirtiesContext
    void testThrownStatusMappingException_IsResolvedAsInternalServerError() {

        StatusMappingException exceptionToMap = new StatusMappingException("Trigger Advice");
        testGrpcAdviceService.setExceptionToSimulate(exceptionToMap);
        Status expectedStatus =
                Status.INTERNAL.withDescription("There was a server error trying to handle an exception");
        Metadata metadata = new Metadata();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);

        assertThat(loggingEventListAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Exception caught during gRPC execution: ", Level.DEBUG))
                .contains(Tuple.tuple(
                        "Exception thrown during invocation of annotated @GrpcExceptionHandler method: ",
                        Level.ERROR));
    }

    @Test
    @DirtiesContext
    void testThrownRootDepth_IsMappedCorrectlyWithRootException() {

        MyRootRuntimeException rootRuntimeException = new MyRootRuntimeException("root exception triggered.");

        testGrpcAdviceService.setExceptionToSimulate(rootRuntimeException);
        Status expectedStatus = Status.DEADLINE_EXCEEDED.withDescription(rootRuntimeException.getMessage());
        Metadata metadata = new Metadata();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @Test
    @DirtiesContext
    void testThrownSecondLevenDepth_IsMappedCorrectlyWithSecondLevelException() {

        SecondLevelException secondLevelException =
                new SecondLevelException("level under first level and second level under root triggered.");

        testGrpcAdviceService.setExceptionToSimulate(secondLevelException);
        Status expectedStatus = Status.ABORTED.withDescription(secondLevelException.getMessage());
        Metadata metadata = new Metadata();

        testGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }



    @BeforeAll
    public static void beforeAll() {
        log.info("--- Starting tests with successful advice exception handling ---");
    }

    @AfterAll
    public static void afterAll() {
        log.info("--- Ending tests with successful advice exception handling ---");
    }

}

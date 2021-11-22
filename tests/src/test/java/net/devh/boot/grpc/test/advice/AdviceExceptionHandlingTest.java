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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import net.devh.boot.grpc.server.error.GrpcExceptionListener;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.FirstLevelException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.LocationToThrow;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.MyRootRuntimeException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.SecondLevelException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.StatusMappingException;
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
        this.loggingEventListAppender = LoggerTestUtil.getListAppenderForClasses(
                GrpcExceptionListener.class,
                GrpcAdviceExceptionHandler.class);
    }


    @Test
    void testOne() {
        testThrownIllegalArgumentException_IsMappedAsStatus(LocationToThrow.METHOD);
    }

    @ParameterizedTest
    @EnumSource(LocationToThrow.class)
    void testThrownIllegalArgumentException_IsMappedAsStatus(final LocationToThrow location) {

        final String message = "Trigger Advice";
        this.testGrpcAdviceService.setExceptionToSimulate(() -> new IllegalArgumentException(message));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus = Status.INVALID_ARGUMENT.withDescription(message);
        final Metadata metadata = new Metadata();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @ParameterizedTest
    @EnumSource(value = LocationToThrow.class, names = {"METHOD", "RESPONSE_OBSERVER"})
    void testThrownAccessControlException_IsMappedAsThrowable(final LocationToThrow location) {

        this.testGrpcAdviceService.setExceptionToSimulate(() -> new AccessControlException("Trigger Advice"));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus = Status.UNKNOWN;
        final Metadata metadata = new Metadata();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @ParameterizedTest
    @EnumSource(value = LocationToThrow.class, names = {"METHOD", "RESPONSE_OBSERVER"})
    void testThrownClassCastException_IsMappedAsStatusRuntimeExceptionAndWithMetadata(final LocationToThrow location) {

        final String message = "Casting with classes failed.";
        this.testGrpcAdviceService.setExceptionToSimulate(() -> new ClassCastException(message));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus = Status.FAILED_PRECONDITION.withDescription(message);
        final Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @ParameterizedTest
    @EnumSource(value = LocationToThrow.class, names = {"METHOD", "RESPONSE_OBSERVER"})
    void testThrownAccountExpiredException_IsNotMappedAndResultsInInvocationException(final LocationToThrow location) {

        // not mapped in GrpcAdviceConfig
        this.testGrpcAdviceService.setExceptionToSimulate(() -> new AccountExpiredException("Trigger Advice"));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus =
                Status.INTERNAL.withDescription("There was a server error trying to handle an exception");
        final Metadata metadata = new Metadata();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);

        assertThat(this.loggingEventListAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Exception caught during gRPC execution: ", Level.DEBUG))
                .contains(Tuple.tuple(
                        "Exception thrown during invocation of annotated @GrpcExceptionHandler method: ",
                        Level.ERROR));
    }

    @ParameterizedTest
    @EnumSource(value = LocationToThrow.class, names = {"METHOD", "RESPONSE_OBSERVER"})
    void testThrownFirstLevelException_IsMappedAsStatusExceptionWithMetadata(final LocationToThrow location) {

        final String message = "Trigger Advice";
        this.testGrpcAdviceService.setExceptionToSimulate(() -> new FirstLevelException(message));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus = Status.NOT_FOUND.withDescription(message);
        final Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @ParameterizedTest
    @EnumSource(value = LocationToThrow.class, names = {"METHOD", "RESPONSE_OBSERVER"})
    void testThrownStatusMappingException_IsResolvedAsInternalServerError(final LocationToThrow location) {

        this.testGrpcAdviceService.setExceptionToSimulate(() -> new StatusMappingException("Trigger Advice"));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus =
                Status.INTERNAL.withDescription("There was a server error trying to handle an exception");
        final Metadata metadata = new Metadata();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);

        assertThat(this.loggingEventListAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Exception caught during gRPC execution: ", Level.DEBUG))
                .contains(Tuple.tuple(
                        "Exception thrown during invocation of annotated @GrpcExceptionHandler method: ",
                        Level.ERROR));
    }

    @ParameterizedTest
    @EnumSource(value = LocationToThrow.class, names = {"METHOD", "RESPONSE_OBSERVER"})
    void testThrownRootDepth_IsMappedCorrectlyWithRootException(final LocationToThrow location) {

        final String message = "root exception triggered.";
        this.testGrpcAdviceService.setExceptionToSimulate(() -> new MyRootRuntimeException(message));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus = Status.DEADLINE_EXCEEDED.withDescription(message);
        final Metadata metadata = new Metadata();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);
    }

    @ParameterizedTest
    @EnumSource(value = LocationToThrow.class, names = {"METHOD", "RESPONSE_OBSERVER"})
    void testThrownSecondLevenDepth_IsMappedCorrectlyWithSecondLevelException(final LocationToThrow location) {

        final String message = "level under first level and second level under root triggered.";
        this.testGrpcAdviceService.setExceptionToSimulate(() -> new SecondLevelException(message));
        this.testGrpcAdviceService.setThrowLocation(location);

        final Status expectedStatus = Status.ABORTED.withDescription(message);
        final Metadata metadata = new Metadata();

        if (!location.isForStreamingOnly()) {
            testUnaryGrpcCallAndVerifyMappedException(expectedStatus, metadata);
        }

        testStreamingGrpcCallAndVerifyMappedException(expectedStatus, metadata);
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

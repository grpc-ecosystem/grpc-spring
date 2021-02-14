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

package net.devh.boot.grpc.test.shutdown;

import static io.grpc.Status.Code.UNAVAILABLE;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.devh.boot.grpc.test.util.FutureAssertions.assertFutureEquals;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertFutureThrowsStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.springframework.context.support.GenericApplicationContext;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessServerBuilder;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import net.devh.boot.grpc.client.channelfactory.InProcessChannelFactory;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.server.WaitingTestService;

/**
 * Tests for {@link InProcessChannelFactory}'s shutdown behavior using an in-process server/channel.
 */
class GrpcChannelLifecycleWithCallsTest {

    private static final int A_BIT_TIME = 100;
    private static final Empty EMPTY = Empty.getDefaultInstance();
    static final SomeType SOME_TYPE = SomeType.getDefaultInstance();

    private static final WaitingTestService service = new WaitingTestService();
    private static final Server server = InProcessServerBuilder.forName("test")
            .addService(service)
            .build();
    private static final GenericApplicationContext applicationContext = new GenericApplicationContext();

    @BeforeAll
    static void beforeAll() throws IOException {
        applicationContext.refresh();
        // Init classes before the actual test, because that is somewhat slow
        applicationContext.start();
        server.start();
        withStub(ZERO, stub -> {

            service.nextDelay(ofMillis(1));
            stub.normal(EMPTY);

            service.awaitAllRequestsArrived();

        });
    }

    @AfterAll
    static void afterAll() {
        server.shutdownNow();
        applicationContext.close();
    }

    @Test
    void testZeroShutdownGracePeriod() {

        final AtomicReference<Future<SomeType>> request = new AtomicReference<>();

        assertTimeoutPreemptively(ofMillis(A_BIT_TIME),
                runWithStub(ZERO, stub -> {

                    // Send request (Takes 5s)
                    service.nextDelay(ofMillis(5000));
                    request.set(stub.normal(EMPTY));

                    service.awaitAllRequestsArrived();

                }));

        // The request did not complete in time
        assertFailedShutdown(request);
    }

    @Test
    void testShortShutdownGracePeriod() {

        final AtomicReference<Future<SomeType>> request1 = new AtomicReference<>();
        final AtomicReference<Future<SomeType>> request2 = new AtomicReference<>();
        final AtomicReference<Future<SomeType>> request3 = new AtomicReference<>();

        assertTimeout(ofMillis(2000 + A_BIT_TIME),
                runWithStub(ofMillis(2000), stub -> {

                    // Send first request (Takes 1s)
                    service.nextDelay(ofMillis(1000));
                    request1.set(stub.normal(EMPTY));

                    // Send second request (Takes 5s)
                    service.nextDelay(ofMillis(5000));
                    request2.set(stub.normal(EMPTY));

                    // Send last request (Takes 1s)
                    service.nextDelay(ofMillis(1000));
                    request3.set(stub.normal(EMPTY));

                    service.awaitAllRequestsArrived();

                }));

        // First one completed
        assertCompleted(request1);

        // The second one did not complete in time
        assertFailedShutdown(request2);

        // Last one completed
        assertCompleted(request3);
    }

    @Test
    void testInfiniteShutdownGracePeriod() {

        final AtomicReference<Future<SomeType>> request = new AtomicReference<>();

        assertTimeoutPreemptively(ofMillis(1000 + A_BIT_TIME),
                runWithStub(ofMillis(-1), stub -> {

                    // Send request (Takes 1s)
                    service.nextDelay(ofMillis(1000));
                    request.set(stub.normal(EMPTY));

                    service.awaitAllRequestsArrived();

                }));

        // Request completed
        assertCompleted(request);
    }

    static Executable runWithStub(final Duration shutdownGracePeriod,
            final ThrowingConsumer<TestServiceFutureStub> executuable) {
        return () -> withStub(shutdownGracePeriod, executuable);
    }

    static void withStub(final Duration shutdownGracePeriod,
            final ThrowingConsumer<TestServiceFutureStub> executuable) {

        final GrpcChannelsProperties properties = new GrpcChannelsProperties();
        properties.getGlobalChannel().setShutdownGracePeriod(shutdownGracePeriod);
        try (final GrpcChannelFactory factory = new InProcessChannelFactory(properties,
                new GlobalClientInterceptorRegistry(applicationContext))) {

            final Channel channel = factory.createChannel("test");
            final TestServiceFutureStub stub = TestServiceGrpc.newFutureStub(channel);
            assertDoesNotThrow(() -> executuable.accept(stub));
        }
    }

    private <T> void assertFailedShutdown(final AtomicReference<Future<T>> request) {
        final Status status = assertFutureThrowsStatus(UNAVAILABLE, request.get(), A_BIT_TIME, MILLISECONDS);
        assertThat(status.getDescription()).contains("shutdownNow");
    }

    private void assertCompleted(final AtomicReference<Future<SomeType>> request) {
        assertFutureEquals(SOME_TYPE, request.get(), A_BIT_TIME, MILLISECONDS);
    }

}

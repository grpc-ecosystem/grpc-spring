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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.services.HealthStatusManager;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.serverfactory.AbstractGrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.server.WaitingTestService;

/**
 * Tests for {@link GrpcServerLifecycle}'s shutdown behavior using an in-process server/channel.
 */
class GrpcServerLifecycleWithCallsTest {

    private static final int A_BIT_TIME = 100;
    private static final Empty EMPTY = Empty.getDefaultInstance();
    private static final SomeType SOME_TYPE = SomeType.getDefaultInstance();

    private static ManagedChannel channel;
    private static TestServiceFutureStub stub;

    @BeforeAll
    static void beforeAll() {
        channel = InProcessChannelBuilder.forName("test").build();
        stub = TestServiceGrpc.newFutureStub(channel);
    }

    @AfterAll
    static void afterAll() {
        channel.shutdownNow();
    }

    @Test
    void testZeroShutdownGracePeriod() {

        withServer(ZERO, (service, lifecycle) -> {
            // Start server
            lifecycle.start();

            // Send request (Takes 5s)
            service.nextDelay(ofMillis(5000));
            final Future<SomeType> request = stub.normal(EMPTY);

            service.awaitAllRequestsArrived();

            // Shutdown and don't wait for graceful shutdown (we give it a short time to finish shutting down)
            assertTimeoutPreemptively(ofMillis(A_BIT_TIME), (Executable) lifecycle::stop);

            // The request did not complete in time
            assertFailedShutdown(request);
        });
    }

    @Test
    void testShortShutdownGracePeriod() {

        withServer(ofMillis(2000), (service, lifecycle) -> {
            // Start server
            lifecycle.start();

            // Send first request (Takes 1s)
            service.nextDelay(ofMillis(1000));
            final Future<SomeType> request1 = stub.normal(EMPTY);

            // Send second request (Takes 5s)
            service.nextDelay(ofMillis(5000));
            final Future<SomeType> request2 = stub.normal(EMPTY);

            // Send last request (Takes 1s)
            service.nextDelay(ofMillis(1000));
            final Future<SomeType> request3 = stub.normal(EMPTY);

            service.awaitAllRequestsArrived();

            // Shutdown and wait a short amount of time to shutdown gracefully
            assertTimeoutPreemptively(ofMillis(2000 + A_BIT_TIME), (Executable) lifecycle::stop);

            // First one completed
            assertCompleted(request1);

            // The second one did not complete in time
            assertFailedShutdown(request2);

            // Last one completed
            assertCompleted(request3);
        });
    }

    @Test
    void testInfiniteShutdownGracePeriod() {

        withServer(ofMillis(-1), (service, lifecycle) -> {
            // Start server
            lifecycle.start();

            // Send request (Takes 1s)
            service.nextDelay(ofMillis(1000));
            final Future<SomeType> request = stub.normal(EMPTY);

            service.awaitAllRequestsArrived();

            // Shutdown and wait for the server to shutdown gracefully
            assertTimeoutPreemptively(ofMillis(1000 + A_BIT_TIME), (Executable) lifecycle::stop);

            // Request completed
            assertCompleted(request);
        });
    }

    void withServer(final Duration gracefulShutdownTimeout,
            final BiConsumer<WaitingTestService, GrpcServerLifecycle> executuable) {
        final GrpcServerFactory factory = new InProcessGrpcServerFactory("test", new GrpcServerProperties());
        assertDoesNotThrow(() -> {
            // TODO: Remove that field
            final Field healthStatusManager = AbstractGrpcServerFactory.class.getDeclaredField("healthStatusManager");
            healthStatusManager.setAccessible(true);
            healthStatusManager.set(factory, new HealthStatusManager());
        });
        try {
            final WaitingTestService service = new WaitingTestService();

            factory.addService(new GrpcServiceDefinition("service", WaitingTestService.class, service.bindService()));

            final GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(factory, gracefulShutdownTimeout);
            try {
                assertDoesNotThrow(() -> executuable.accept(service, lifecycle));
            } finally {
                lifecycle.stop();
            }
        } finally {
            factory.destroy();
        }
    }

    private <T> void assertFailedShutdown(final Future<T> request) {
        final Status status = assertFutureThrowsStatus(UNAVAILABLE, request, A_BIT_TIME, MILLISECONDS);
        assertThat(status.getDescription()).contains("shutdownNow");
    }

    private void assertCompleted(final Future<SomeType> request) {
        assertFutureEquals(SOME_TYPE, request, A_BIT_TIME, MILLISECONDS);
    }

}

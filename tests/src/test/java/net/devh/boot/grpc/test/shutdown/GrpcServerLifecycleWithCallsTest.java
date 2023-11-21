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

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
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

    private final ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

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
        final WaitingTestService service = new WaitingTestService();

        factory.addService(new GrpcServiceDefinition("service", WaitingTestService.class, service.bindService()));

        final GrpcServerLifecycle lifecycle =
                new GrpcServerLifecycle(factory, gracefulShutdownTimeout, this.eventPublisher);
        try {
            assertDoesNotThrow(() -> executuable.accept(service, lifecycle));
        } finally {
            lifecycle.stop();
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

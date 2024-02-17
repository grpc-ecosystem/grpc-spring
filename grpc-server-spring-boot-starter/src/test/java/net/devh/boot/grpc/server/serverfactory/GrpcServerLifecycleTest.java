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

package net.devh.boot.grpc.server.serverfactory;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;

import io.grpc.Server;
import net.devh.boot.grpc.server.event.GrpcServerShutdownEvent;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import net.devh.boot.grpc.server.event.GrpcServerTerminatedEvent;

/**
 * Tests for {@link GrpcServerLifecycle}.
 */
class GrpcServerLifecycleTest {

    private final GrpcServerFactory factory = mock(GrpcServerFactory.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    @BeforeEach
    void beforeEach() {
        reset(this.factory, this.eventPublisher);
        when(this.factory.getAddress()).thenReturn("test");
        when(this.factory.getPort()).thenReturn(-1);
    }

    @Test
    void testNoGraceShutdown() {
        // The server takes 5s seconds to shutdown
        final TestServer server = new TestServer(5000);
        when(this.factory.createServer()).thenReturn(server);

        // But we won't wait
        final GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(this.factory, ZERO, this.eventPublisher);

        lifecycle.start();
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerStartedEvent.class));

        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());

        // So the shutdown should complete near instantly
        assertTimeoutPreemptively(ofMillis(100), (Executable) lifecycle::stop);
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerShutdownEvent.class));
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerTerminatedEvent.class));

        assertTrue(server.isShutdown());
        assertTrue(server.isTerminated());
    }

    @Test
    void testGracefulShutdown() {

        // The server takes 2s seconds to shutdown
        final TestServer server = new TestServer(2000);
        when(this.factory.createServer()).thenReturn(server);

        // And we give it 5s to shutdown
        final GrpcServerLifecycle lifecycle =
                new GrpcServerLifecycle(this.factory, ofMillis(5000), this.eventPublisher);

        lifecycle.start();
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerStartedEvent.class));

        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());

        // So it should finish within 5.1 seconds
        assertTimeout(ofMillis(5100), (Executable) lifecycle::stop);
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerShutdownEvent.class));
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerTerminatedEvent.class));

        assertTrue(server.isShutdown());
        assertTrue(server.isTerminated());

    }

    @Test
    void testAwaitShutdown() {

        // The server takes 2s seconds to shutdown
        final TestServer server = new TestServer(5000);
        when(this.factory.createServer()).thenReturn(server);

        // And we give it infinite time to shutdown
        final GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(this.factory, ofSeconds(-1), this.eventPublisher);

        lifecycle.start();
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerStartedEvent.class));

        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());

        final long start = System.currentTimeMillis();

        lifecycle.stop();
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerShutdownEvent.class));
        verify(this.eventPublisher).publishEvent(ArgumentMatchers.any(GrpcServerTerminatedEvent.class));

        final long duration = System.currentTimeMillis() - start;
        // We waited for the entire duration
        assertThat(duration).isBetween(5000L, 5100L);

        assertTrue(server.isShutdown());
        assertTrue(server.isTerminated());

    }

    public class TestServer extends Server {

        private final long shutdownDelayMillis;

        private boolean isShutdown = true;
        private CountDownLatch countDown = null;

        public TestServer(final long shutdownDelayMillis) {
            this.shutdownDelayMillis = shutdownDelayMillis;
        }

        @Override
        public Server start() throws IOException {
            this.countDown = new CountDownLatch(1);
            this.isShutdown = false;
            return this;
        }

        @Override
        public Server shutdown() {
            this.isShutdown = true;
            final Thread t = new Thread(() -> {
                try {
                    Thread.sleep(this.shutdownDelayMillis);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                shutdownNow();
            });
            t.setName("test-server-shutdown-delay");
            t.setDaemon(true);
            t.start();
            return this;
        }

        @Override
        public Server shutdownNow() {
            this.isShutdown = true;
            final CountDownLatch localCountDown = this.countDown;
            this.countDown = null;
            if (localCountDown != null) {
                localCountDown.countDown();
            }
            return this;
        }

        @Override
        public boolean isShutdown() {
            return this.isShutdown;
        }

        @Override
        public boolean isTerminated() {
            return this.countDown == null;
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return this.countDown.await(timeout, unit);
        }

        @Override
        public void awaitTermination() throws InterruptedException {
            this.countDown.await();
        }

    }

}

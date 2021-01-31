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

package net.devh.boot.grpc.server.serverfactory;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import io.grpc.Server;

/**
 * Tests for {@link GrpcServerLifecycle}.
 */
class GrpcServerLifecycleTest {

    private final GrpcServerFactory factory = mock(GrpcServerFactory.class);

    @BeforeEach
    void beforeEach() {
        reset(this.factory);
        when(this.factory.getAddress()).thenReturn("test");
        when(this.factory.getPort()).thenReturn(-1);

    }

    @Test
    void testNoGraceShutdown() {
        // The server takes 5s seconds to shutdown
        final TestServer server = new TestServer(5000);
        when(this.factory.createServer()).thenReturn(server);

        // But we won't wait
        final GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(this.factory, ZERO);

        lifecycle.start();

        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());

        // So the shutdown should complete near instantly
        assertTimeoutPreemptively(ofMillis(100), (Executable) lifecycle::stop);

        assertTrue(server.isShutdown());
        assertTrue(server.isTerminated());
    }

    @Test
    void testGracefulShutdown() {

        // The server takes 2s seconds to shutdown
        final TestServer server = new TestServer(2000);
        when(this.factory.createServer()).thenReturn(server);

        // And we give it 5s to shutdown
        final GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(this.factory, ofMillis(5000));

        lifecycle.start();

        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());

        // So it should finish within 5.1 seconds
        assertTimeout(ofMillis(5100), (Executable) lifecycle::stop);

        assertTrue(server.isShutdown());
        assertTrue(server.isTerminated());

    }

    @Test
    void testAwaitShutdown() {

        // The server takes 2s seconds to shutdown
        final TestServer server = new TestServer(5000);
        when(this.factory.createServer()).thenReturn(server);

        // And we give it infinite time to shutdown
        final GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(this.factory, ofSeconds(-1));

        lifecycle.start();

        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());

        final long start = System.currentTimeMillis();

        lifecycle.stop();

        final long duration = System.currentTimeMillis() - start;
        // We waited for the entire duration
        assertThat(duration).isBetween(5000L, 5100L);

        assertTrue(server.isShutdown());
        assertTrue(server.isTerminated());

    }

    @Test
    void testInterruptShutdown() {

        // The server takes 60s seconds to shutdown
        final TestServer server = new TestServer(60000);
        when(this.factory.createServer()).thenReturn(server);

        // And we give it infinite time to shutdown
        final GrpcServerLifecycle lifecycle = new GrpcServerLifecycle(this.factory, ofSeconds(-1));

        lifecycle.start();

        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());

        try {
            // But we are in a hurry, so we interrupt it after 2s
            assertTimeoutPreemptively(ofMillis(2000), (Executable) lifecycle::stop);
            fail("Did not wait for shutdown to complete");
        } catch (final AssertionFailedError e) {
            // We failed due to the timeout/interrupt
            assertThat(e).getCause().matches(t -> "ExecutionTimeoutException".equals(t.getClass().getSimpleName()));
        }

        // But the server is still properly terminated
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

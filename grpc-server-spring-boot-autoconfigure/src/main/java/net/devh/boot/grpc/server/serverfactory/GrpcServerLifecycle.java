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

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.SmartLifecycle;

import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle bean that automatically starts and stops the grpc server.
 *
 * @author Michael (yidongnan@gmail.com)
 */
@Slf4j
public class GrpcServerLifecycle implements SmartLifecycle {

    private static AtomicInteger serverCounter = new AtomicInteger(-1);

    private final GrpcServerFactory factory;
    private final Duration gracefulShutdownTimeout;

    private Server server;

    /**
     * Creates a new GrpcServerLifecycle
     *
     * @param factory The server factory to use.
     * @param gracefulShutdownTimeout The time to wait for the server to gracefully shut down.
     */
    public GrpcServerLifecycle(final GrpcServerFactory factory, final Duration gracefulShutdownTimeout) {
        this.factory = requireNonNull(factory, "factory");
        this.gracefulShutdownTimeout = requireNonNull(gracefulShutdownTimeout, "gracefulShutdownTimeout");
    }

    @Override
    public void start() {
        try {
            createAndStartGrpcServer();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to start the grpc server", e);
        }
    }

    @Override
    public void stop() {
        stopAndReleaseGrpcServer();
    }

    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return this.server != null && !this.server.isShutdown();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Creates and starts the grpc server.
     *
     * @throws IOException If the server is unable to bind the port.
     */
    protected void createAndStartGrpcServer() throws IOException {
        if (this.server == null) {
            final Server localServer = this.factory.createServer();
            this.server = localServer;
            localServer.start();
            log.info("gRPC Server started, listening on address: " + this.factory.getAddress() + ", port: "
                    + this.factory.getPort());

            // Prevent the JVM from shutting down while the server is running
            final Thread awaitThread = new Thread(() -> {
                try {
                    localServer.awaitTermination();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            awaitThread.setName("grpc-server-container-" + (serverCounter.incrementAndGet()));
            awaitThread.setDaemon(false);
            awaitThread.start();
        }
    }

    /**
     * Initiates an orderly shutdown of the grpc server and releases the references to the server. This call waits for
     * the server to be completely shut down.
     */
    protected void stopAndReleaseGrpcServer() {
        final Server localServer = this.server;
        if (localServer != null) {
            final long millis = this.gracefulShutdownTimeout.toMillis();
            log.debug("Initiating gRPC server shutdown");
            localServer.shutdown();
            // Wait for the server to shutdown completely before continuing with destroying the spring context
            try {
                if (millis > 0) {
                    localServer.awaitTermination(millis, MILLISECONDS);
                } else if (millis == 0) {
                    // Do not wait
                } else {
                    // Wait infinitely
                    localServer.awaitTermination();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                localServer.shutdownNow();
                this.server = null;
            }
            log.info("Completed gRPC server shutdown");
        }
    }

}

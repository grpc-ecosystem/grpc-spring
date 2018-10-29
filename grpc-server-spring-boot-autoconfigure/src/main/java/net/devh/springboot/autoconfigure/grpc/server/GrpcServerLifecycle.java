/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.springboot.autoconfigure.grpc.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.SmartLifecycle;

import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle bean that automatically starts and stops the grpc server.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Slf4j
public class GrpcServerLifecycle implements SmartLifecycle {
    private static AtomicInteger serverCounter = new AtomicInteger(-1);

    private volatile Server server;
    private volatile int phase = Integer.MAX_VALUE;
    private final GrpcServerFactory factory;

    public GrpcServerLifecycle(final GrpcServerFactory factory) {
        this.factory = factory;
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
        this.stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return this.server == null ? false : !this.server.isShutdown();
    }

    @Override
    public int getPhase() {
        return this.phase;
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
        final Server localServer = this.server;
        if (localServer == null) {
            this.server = this.factory.createServer();
            this.server.start();
            log.info("gRPC Server started, listening on address: " + this.factory.getAddress() + ", port: "
                    + this.factory.getPort());

            final Thread awaitThread = new Thread("container-" + (serverCounter.incrementAndGet())) {

                @Override
                public void run() {
                    try {
                        GrpcServerLifecycle.this.server.awaitTermination();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            };
            awaitThread.setDaemon(false);
            awaitThread.start();
        }
    }

    /**
     * Initiates an orderly shutdown of the grpc server and releases the references to the server. This call does not
     * wait for the server to be completely shut down.
     */
    protected void stopAndReleaseGrpcServer() {
        factory.destroy();
        Server localServer = this.server;
        if (localServer != null) {
            localServer.shutdown();
            this.server = null;
            log.info("gRPC server shutdown.");
        }
    }

}

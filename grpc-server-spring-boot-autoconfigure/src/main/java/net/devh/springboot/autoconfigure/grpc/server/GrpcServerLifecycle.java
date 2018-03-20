package net.devh.springboot.autoconfigure.grpc.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.SmartLifecycle;

import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Slf4j
public class GrpcServerLifecycle implements SmartLifecycle {
    private static AtomicInteger serverCounter = new AtomicInteger(-1);

    private volatile Server server;
    private volatile int phase = Integer.MAX_VALUE;
    private final GrpcServerFactory factory;

    public GrpcServerLifecycle(GrpcServerFactory factory) {
        this.factory = factory;
    }

    @Override
    public void start() {
        try {
            createAndStartGrpcServer();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop() {
        stopAndReleaseGrpcServer();
    }

    @Override
    public void stop(Runnable callback) {
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

    protected void createAndStartGrpcServer() throws IOException {
        Server localServer = this.server;
        if (localServer == null) {
            this.server = this.factory.createServer();
            this.server.start();
            log.info("gRPC Server started, listening on address: " + this.factory.getAddress() + ", port: " + this.factory.getPort());

            Thread awaitThread = new Thread(
                    "container-" + (serverCounter.incrementAndGet())) {

                @Override
                public void run() {
                    try {
                        GrpcServerLifecycle.this.server.awaitTermination();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            };
            awaitThread.setDaemon(false);
            awaitThread.start();
        }
    }

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

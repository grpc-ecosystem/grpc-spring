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

package net.devh.boot.grpc.test.server;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceImplBase;

/**
 * A test service implementation that spends a configurable amount of time processing a request.
 */
public class WaitingTestService extends TestServiceImplBase {

    // Queue length = 1 -> Ability to control/await server calls
    private final BlockingQueue<Long> delays = new ArrayBlockingQueue<>(1);

    /**
     * The next call will wait the configured amount of time before completing. Allows exactly one call to process. May
     * only queue up to one call.
     *
     * @param delay The delay to wait for.
     */
    public synchronized void nextDelay(final Duration delay) {
        assertTimeoutPreemptively(
                ofSeconds(1),
                () -> assertDoesNotThrow(() -> this.delays.put(delay.toMillis())),
                "Failed to queue delay");
    }

    /**
     * Waits until all request have started processing on the server.
     */
    public synchronized void awaitAllRequestsArrived() {
        // Just try to set a value
        nextDelay(ofMillis(-1));
        this.delays.clear();
    }

    @Override
    public void normal(final Empty request, final StreamObserver<SomeType> responseObserver) {
        // Simulate processing time
        assertDoesNotThrow(this::sleep);
        responseObserver.onNext(SomeType.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private void sleep() throws InterruptedException {
        final long delay = assertTimeoutPreemptively(ofSeconds(1), () -> this.delays.take());
        if (delay <= 0) {
            throw new IllegalStateException("Bad delay: " + delay);
        }
        Thread.sleep(delay);
    }

}

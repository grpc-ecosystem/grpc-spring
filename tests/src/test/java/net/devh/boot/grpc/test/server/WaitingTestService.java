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

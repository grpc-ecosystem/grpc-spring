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

package net.devh.boot.grpc.server.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.grpc.stub.StreamObserver;

class AwaitableStreamObserver<V> implements StreamObserver<V> {

    private final CountDownLatch doneLatch = new CountDownLatch(1);
    private final List<V> results = new ArrayList<>();
    private volatile Throwable error;

    @Override
    public void onNext(final V value) {
        this.results.add(value);
    }

    @Override
    public void onError(final Throwable t) {
        this.error = t;
        this.doneLatch.countDown();
    }

    @Override
    public void onCompleted() {
        this.doneLatch.countDown();
    }

    public V getFirst() throws InterruptedException {
        this.doneLatch.await();
        if (this.error != null) {
            throw new IllegalStateException("Request failed with unexpected error", this.error);
        }
        if (this.results.isEmpty()) {
            throw new IllegalStateException("Requested completed without response");
        }
        return this.results.get(0);
    }

    public V getSingle() throws InterruptedException {
        this.doneLatch.await();
        if (this.error != null) {
            throw new IllegalStateException("Request failed with unexpected error", this.error);
        }
        if (this.results.isEmpty()) {
            throw new IllegalStateException("Requested completed without response");
        }
        if (this.results.size() != 1) {
            throw new IllegalStateException(
                    "Request completed with more than one response - Got " + this.results.size());
        }
        return this.results.get(0);
    }

    public List<V> getAll() throws InterruptedException {
        this.doneLatch.await();
        if (this.error != null) {
            throw new IllegalStateException("Request failed with unexpected error", this.error);
        }
        return this.results;
    }

    public Throwable getError() throws InterruptedException {
        this.doneLatch.await();
        if (this.error == null) {
            throw new IllegalStateException("Request completed without error");
        }
        return this.error;
    }

}

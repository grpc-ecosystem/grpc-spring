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

package net.devh.boot.grpc.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.grpc.stub.StreamObserver;

public class AwaitableStreamObserver<V> implements StreamObserver<V> {

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

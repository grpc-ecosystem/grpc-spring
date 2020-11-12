/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.metric;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.ServerCall.Listener;
import io.grpc.Status;
import io.micrometer.core.instrument.Counter;

/**
 * A simple forwarding server call listener that collects metrics for micrometer.
 *
 * @param <Q> The type of message received one or more times from the client.
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class MetricCollectingServerCallListener<Q> extends SimpleForwardingServerCallListener<Q> {

    private final Counter requestCounter;
    private final Supplier<Status.Code> responseCodeSupplier;
    private final Consumer<Status.Code> responseStatusTiming;

    /**
     * Creates a new delegating ServerCallListener that will wrap the given server call listener to collect metrics.
     *
     * @param delegate The original listener to wrap.
     * @param requestCounter The counter for incoming requests.
     * @param responseCodeSupplier The supplier of the response code.
     * @param responseStatusTiming The consumer used to time the processing duration along with a response status.
     */

    public MetricCollectingServerCallListener(
            final Listener<Q> delegate,
            final Counter requestCounter,
            final Supplier<Status.Code> responseCodeSupplier,
            final Consumer<Status.Code> responseStatusTiming) {

        super(delegate);
        this.requestCounter = requestCounter;
        this.responseCodeSupplier = responseCodeSupplier;
        this.responseStatusTiming = responseStatusTiming;
    }

    @Override
    public void onMessage(final Q requestMessage) {
        this.requestCounter.increment();
        super.onMessage(requestMessage);
    }

    @Override
    public void onComplete() {
        report(this.responseCodeSupplier.get());
        super.onComplete();
    }

    @Override
    public void onCancel() {
        report(Status.Code.CANCELLED);
        super.onCancel();
    }

    private void report(final Status.Code code) {
        this.responseStatusTiming.accept(code);
    }

}

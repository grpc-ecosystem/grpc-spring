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

package net.devh.boot.grpc.client.metric;

import java.util.function.Consumer;

import io.grpc.ClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.Status;
import io.micrometer.core.instrument.Counter;

/**
 * A simple forwarding client call listener that collects metrics for micrometer.
 *
 * @param <A> The type of message received one or more times from the server.
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class MetricCollectingClientCallListener<A> extends SimpleForwardingClientCallListener<A> {

    private final Counter responseCounter;
    private final Consumer<Status.Code> processingDurationTiming;

    /**
     * Creates a new delegating ClientCallListener that will wrap the given client call listener to collect metrics.
     *
     * @param delegate The original call to wrap.
     * @param responseCounter The counter for incoming responses.
     * @param processingDurationTiming The consumer used to time the processing duration along with a response status.
     */
    public MetricCollectingClientCallListener(
            final ClientCall.Listener<A> delegate,
            final Counter responseCounter,
            final Consumer<Status.Code> processingDurationTiming) {

        super(delegate);
        this.responseCounter = responseCounter;
        this.processingDurationTiming = processingDurationTiming;
    }

    @Override
    public void onClose(final Status status, final Metadata metadata) {
        this.processingDurationTiming.accept(status.getCode());
        super.onClose(status, metadata);
    }

    @Override
    public void onMessage(final A responseMessage) {
        this.responseCounter.increment();
        super.onMessage(responseMessage);
    }

}

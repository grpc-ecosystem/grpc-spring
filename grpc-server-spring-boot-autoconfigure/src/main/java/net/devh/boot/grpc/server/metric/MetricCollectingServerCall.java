/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

import java.util.function.Function;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * A simple forwarding server call that collects metrics for micrometer.
 *
 * @param <Q> The type of message received one or more times from the client.
 * @param <A> The type of message sent one or more times to the client.
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class MetricCollectingServerCall<Q, A> extends SimpleForwardingServerCall<Q, A> {

    private final Counter responseCounter;
    private final Function<Code, Timer> timerFunction;
    private final Timer.Sample timerSample;
    private final Function<Code, Counter> resultFunction;

    /**
     * Creates a new delegating ServerCall that will wrap the given server call to collect metrics.
     *
     * @param delegate The original call to wrap.
     * @param registry The registry to save the metrics to.
     * @param responseCounter The counter for incoming responses.
     * @param timerFunction A function that will return a timer for a given status code.
     */
    public MetricCollectingServerCall(final ServerCall<Q, A> delegate, final MeterRegistry registry,
            final Counter responseCounter,
            final Function<Code, Timer> timerFunction,
            final Function<Code, Counter> resultFunction) {
        super(delegate);
        this.responseCounter = responseCounter;
        this.timerFunction = timerFunction;
        this.timerSample = Timer.start(registry);
        this.resultFunction = resultFunction;
    }

    @Override
    public void close(final Status status, final Metadata responseHeaders) {
        this.timerSample.stop(this.timerFunction.apply(status.getCode()));
        this.resultFunction.apply(status.getCode()).increment();
        super.close(status, responseHeaders);
    }

    @Override
    public void sendMessage(final A responseMessage) {
        this.responseCounter.increment();
        super.sendMessage(responseMessage);
    }

}

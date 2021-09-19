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

package net.devh.boot.grpc.server.error;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * An exception handler for errors in the grpc call execution (For both the grpc method implementations and the
 * {@link StreamObserver}s used to process incoming messages and sending outgoing errors).
 *
 * <p>
 * Implementations must:
 * </p>
 *
 * <ul>
 * <li>Call {@link ServerCall#close(Status, Metadata)} before returning</li>
 * <li>Not throw (any thrown errors must be caught)</li>
 * <li>Not keep a reference to the call instance after the call</li>
 * </ul>
 */
public interface GrpcExceptionResponseHandler {

    /**
     * Handles an exception by closing the call with an appropriate {@link Status}.
     *
     * @param serverCall The server call used to send the response status.
     * @param error The error to handle.
     */
    void handleError(final ServerCall<?, ?> serverCall, final Throwable error);

}

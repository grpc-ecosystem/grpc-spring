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

package net.devh.boot.grpc.web.bridge;

import static java.util.Objects.requireNonNull;

import java.util.List;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class GrpcMethodResult<T> {

    public static <T> GrpcMethodResult<T> from(final StatusRuntimeException e) {
        return new GrpcMethodResult<>(e.getStatus(), e.getTrailers());
    }

    private final Status status;
    private final Metadata headers;
    private final List<T> messages;

    /**
     * Creates a new grpc method result for a failed call.
     *
     * @param status The result status of the call.
     */
    public GrpcMethodResult(final Status status) {
        this(status, null);
    }

    /**
     * Creates a new grpc method result for a failed call.
     *
     * @param status The result status of the call.
     * @param headers The headers of the call. Null will be replaced by a new empty instance.
     */
    public GrpcMethodResult(final Status status, final Metadata headers) {
        this(status, headers, null);
    }

    /**
     * Creates a new grpc method result with the given messages.
     *
     * @param status The result status of the call.
     * @param headers The headers of the call. Null will be replaced by a new empty instance.
     * @param messages The result messages of call or null if there are no results.
     */
    public GrpcMethodResult(final Status status, final Metadata headers, final List<T> messages) {
        this.status = requireNonNull(status, "status");
        this.headers = headers == null ? new Metadata() : headers;
        this.messages = messages;
    }

    /**
     * Gets the result status of the grpc call.
     *
     * @return The result status of the call.
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Gets whether the grpc method call was successful.
     *
     * @return True, if the call was successful (code = OK). False otherwise.
     */
    public boolean wasSuccessful() {
        return this.status.getCode() == Status.Code.OK;
    }

    /**
     * Gets the response headers that were delivered during the request or its completion.
     *
     * @return The response headers of the call.
     */
    public Metadata getHeaders() {
        return this.headers;
    }

    /**
     * Gets the messages of the call in the same order they were delivered.
     *
     * @return The results of the call.
     */
    public List<T> getMessages() {
        if (!wasSuccessful()) {
            throw new IllegalStateException("Cannot access results of failed call");
        }
        return this.messages;
    }

}

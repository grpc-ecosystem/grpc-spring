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

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.grpc.Status.Code;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionHandler;

/**
 * Specialized {@link ServerCall} for server side exception handling.
 *
 * @param <ReqT> gRPC request type
 * @param <RespT> gRPC response type
 * @see GrpcExceptionInterceptor
 * @see GrpcAdviceExceptionHandler
 */
public class GrpcExceptionServerCall<ReqT, RespT> extends SimpleForwardingServerCall<ReqT, RespT> {

    private final GrpcExceptionResponseHandler exceptionHandler;

    /**
     * Creates a new exception handling grpc server call.
     *
     * @param delegate The call to delegate to (Required).
     * @param exceptionHandler The exception handler to use (Required).
     */
    protected GrpcExceptionServerCall(
            final ServerCall<ReqT, RespT> delegate,
            final GrpcExceptionResponseHandler exceptionHandler) {

        super(delegate);
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void close(final Status status, final Metadata trailers) {
        // For error responses sent via StreamObserver#onError
        if (status.getCode() == Code.UNKNOWN && status.getCause() != null) {
            final Throwable cause = status.getCause();
            this.exceptionHandler.handleError(delegate(), cause);
        } else {
            super.close(status, trailers);
        }
    }

}

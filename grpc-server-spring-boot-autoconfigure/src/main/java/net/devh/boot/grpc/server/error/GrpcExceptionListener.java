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

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionHandler;

/**
 * {@link io.grpc.ServerCall.Listener ServerCall.Listener} for server side exception handling.
 *
 * @param <ReqT> gRPC request type
 * @param <RespT> gRPC response type
 * @see GrpcExceptionInterceptor
 * @see GrpcAdviceExceptionHandler
 */
public class GrpcExceptionListener<ReqT, RespT> extends SimpleForwardingServerCallListener<ReqT> {

    private final GrpcExceptionResponseHandler exceptionHandler;
    private final ServerCall<ReqT, RespT> serverCall;

    /**
     * Creates a new exception handling grpc server call listener.
     *
     * @param delegate The listener to delegate to (Required).
     * @param serverCall The server call to used to send the error responses (Required).
     * @param exceptionHandler The exception handler to use (Required).
     */
    protected GrpcExceptionListener(
            final Listener<ReqT> delegate,
            final ServerCall<ReqT, RespT> serverCall,
            final GrpcExceptionResponseHandler exceptionHandler) {

        super(delegate);
        this.serverCall = serverCall;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void onMessage(final ReqT message) {
        try {
            super.onMessage(message);
        } catch (final Throwable error) {
            // For errors thrown in the request's StreamObserver#onNext
            this.exceptionHandler.handleError(this.serverCall, error);
        }
    }

    @Override
    public void onHalfClose() {
        try {
            super.onHalfClose();
        } catch (final Throwable error) {
            // For errors from unary grpc method implementation methods directly (Not via StreamObserver)
            // For errors thrown in the request's StreamObserver#onCompleted
            this.exceptionHandler.handleError(this.serverCall, error);
        }
    }

}

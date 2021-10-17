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

import static java.util.Objects.requireNonNull;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.advice.GrpcAdviceExceptionHandler;

/**
 * Interceptor to use for global exception handling. Every raised {@link Throwable} is caught and being processed.
 * Actual processing of exception is in {@link GrpcExceptionListener}.
 * <p>
 *
 * @see GrpcAdviceExceptionHandler
 * @see GrpcExceptionListener
 */
public class GrpcExceptionInterceptor implements ServerInterceptor {

    private final GrpcExceptionResponseHandler exceptionHandler;

    /**
     * Creates a new GrpcAdviceExceptionInterceptor.
     *
     * @param grpcAdviceExceptionHandler The exception handler to use.
     */
    public GrpcExceptionInterceptor(final GrpcExceptionResponseHandler grpcAdviceExceptionHandler) {
        this.exceptionHandler = requireNonNull(grpcAdviceExceptionHandler, "grpcAdviceExceptionHandler");
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            final ServerCall<ReqT, RespT> call,
            final Metadata headers,
            final ServerCallHandler<ReqT, RespT> next) {

        try {
            final GrpcExceptionServerCall<ReqT, RespT> handledCall =
                    new GrpcExceptionServerCall<>(call, this.exceptionHandler);
            final Listener<ReqT> delegate = next.startCall(handledCall, headers);
            return new GrpcExceptionListener<>(delegate, call, this.exceptionHandler);

        } catch (final Throwable error) {
            // For errors from grpc method implementation methods directly (Not via StreamObserver)
            this.exceptionHandler.handleError(call, error); // Required to close streaming calls
            return noOpCallListener();
        }
    }

    /**
     * Creates a new no-op call listener because you can neither return null nor throw an exception in
     * {@link #interceptCall(ServerCall, Metadata, ServerCallHandler)}.
     *
     * @param <ReqT> The type of the request.
     * @return The newly created dummy listener.
     */
    protected <ReqT> Listener<ReqT> noOpCallListener() {
        return new Listener<ReqT>() {};
    }

}

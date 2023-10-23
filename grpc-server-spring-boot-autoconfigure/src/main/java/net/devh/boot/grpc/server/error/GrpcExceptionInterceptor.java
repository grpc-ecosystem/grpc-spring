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

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

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

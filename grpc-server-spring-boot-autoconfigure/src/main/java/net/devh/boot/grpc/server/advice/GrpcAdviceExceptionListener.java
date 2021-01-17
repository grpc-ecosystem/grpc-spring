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

package net.devh.boot.grpc.server.advice;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * In case an exception is thrown inside {@link #onHalfClose()}, it is being handled by invoking annotated methods with
 * {@link GrpcExceptionHandler @GrpcExceptionHandler}. On successful invocation proper exception handling is done.
 * <p>
 * <b>Note:</b> In case of raised exceptions by implementation a {@link Status#INTERNAL} is returned in
 * {@link #handleThrownExceptionByImplementation(Throwable)}.
 *
 * @param <ReqT> gRPC request type
 * @param <RespT> gRPC response type
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcAdviceExceptionHandler
 */
@Slf4j
public class GrpcAdviceExceptionListener<ReqT, RespT> extends SimpleForwardingServerCallListener<ReqT> {

    private final GrpcAdviceExceptionHandler exceptionHandler;
    private final ServerCall<ReqT, RespT> serverCall;

    protected GrpcAdviceExceptionListener(
            Listener<ReqT> delegate,
            ServerCall<ReqT, RespT> serverCall,
            GrpcAdviceExceptionHandler grpcAdviceExceptionHandler) {
        super(delegate);
        this.serverCall = serverCall;
        this.exceptionHandler = grpcAdviceExceptionHandler;
    }

    @Override
    public void onHalfClose() {
        try {
            super.onHalfClose();

        } catch (Throwable throwable) {
            handleCaughtException(throwable);
        }
    }

    private void handleCaughtException(Throwable throwable) {
        try {
            Object mappedReturnType = exceptionHandler.handleThrownException(throwable);
            Status status = resolveStatus(mappedReturnType).withCause(throwable);
            Metadata metadata = resolveMetadata(mappedReturnType);

            serverCall.close(status, metadata);
        } catch (Throwable throwableWhileResolving) {
            handleThrownExceptionByImplementation(throwableWhileResolving);
        }
    }

    private Status resolveStatus(Object mappedReturnType) {
        if (mappedReturnType instanceof Status) {
            return (Status) mappedReturnType;
        } else if (mappedReturnType instanceof Throwable) {
            return Status.fromThrowable((Throwable) mappedReturnType);
        }
        throw new IllegalStateException(String.format(
                "Error for mapped return type [%s] inside @GrpcAdvice, it has to be of type: "
                        + "[Status, StatusException, StatusRuntimeException, Throwable] ",
                mappedReturnType));
    }

    private Metadata resolveMetadata(Object mappedReturnType) {
        Metadata result = null;
        if (mappedReturnType instanceof StatusException) {
            StatusException statusException = (StatusException) mappedReturnType;
            result = statusException.getTrailers();
        } else if (mappedReturnType instanceof StatusRuntimeException) {
            StatusRuntimeException statusException = (StatusRuntimeException) mappedReturnType;
            result = statusException.getTrailers();
        }
        return (result == null) ? new Metadata() : result;
    }

    private void handleThrownExceptionByImplementation(Throwable throwable) {
        log.error("Exception thrown during invocation of annotated @GrpcExceptionHandler method: ", throwable);
        serverCall.close(Status.INTERNAL.withCause(throwable)
                .withDescription("There was a server error trying to handle an exception"), new Metadata());
    }

}

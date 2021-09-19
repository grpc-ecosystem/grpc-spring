
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

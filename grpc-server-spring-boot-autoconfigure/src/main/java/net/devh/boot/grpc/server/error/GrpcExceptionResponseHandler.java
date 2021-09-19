
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

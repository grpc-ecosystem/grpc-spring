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

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

package net.devh.boot.grpc.server.security.interceptors;

import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * Server interceptor that translates any {@link AuthenticationException} and {@link AccessDeniedException} to
 * appropriate grpc status responses.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@GrpcGlobalServerInterceptor
@Order(InterceptorOrder.ORDER_SECURITY_EXCEPTION_HANDLING)
public class ExceptionTranslatingServerInterceptor implements ServerInterceptor {

    /**
     * A constant that contains the response message for unauthenticated calls.
     */
    public static final String UNAUTHENTICATED_DESCRIPTION = "Authentication failed";
    /**
     * A constant that contains the response message for calls with insufficient permissions.
     */
    public static final String ACCESS_DENIED_DESCRIPTION = "Access denied";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call,
            final Metadata headers,
            final ServerCallHandler<ReqT, RespT> next) {
        try {
            // Streaming calls error out here
            return new ExceptionTranslatorServerCallListener<>(next.startCall(call, headers), call);
        } catch (final AuthenticationException aex) {
            closeCallUnauthenticated(call, aex);
            return noOpCallListener();
        } catch (final AccessDeniedException aex) {
            closeCallAccessDenied(call, aex);
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

    /**
     * Close the call with {@link Status#UNAUTHENTICATED}.
     *
     * @param call The call to close.
     * @param aex The exception that was the cause.
     */
    protected void closeCallUnauthenticated(final ServerCall<?, ?> call, final AuthenticationException aex) {
        log.debug(UNAUTHENTICATED_DESCRIPTION, aex);
        call.close(Status.UNAUTHENTICATED.withCause(aex).withDescription(UNAUTHENTICATED_DESCRIPTION), new Metadata());
    }

    /**
     * Close the call with {@link Status#PERMISSION_DENIED}.
     *
     * @param call The call to close.
     * @param aex The exception that was the cause.
     */
    protected void closeCallAccessDenied(final ServerCall<?, ?> call, final AccessDeniedException aex) {
        log.debug(ACCESS_DENIED_DESCRIPTION, aex);
        call.close(Status.PERMISSION_DENIED.withCause(aex).withDescription(ACCESS_DENIED_DESCRIPTION), new Metadata());
    }

    /**
     * Server call listener that catches and handles exceptions in {@link #onHalfClose()}.
     *
     * @param <ReqT> The type of the request.
     * @param <RespT> The type of the response.
     */
    private class ExceptionTranslatorServerCallListener<ReqT, RespT> extends SimpleForwardingServerCallListener<ReqT> {

        private final ServerCall<ReqT, RespT> call;

        protected ExceptionTranslatorServerCallListener(final Listener<ReqT> delegate,
                final ServerCall<ReqT, RespT> call) {
            super(delegate);
            this.call = call;
        }

        @Override
        // Unary calls error out here
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } catch (final AuthenticationException aex) {
                closeCallUnauthenticated(this.call, aex);
            } catch (final AccessDeniedException aex) {
                closeCallAccessDenied(this.call, aex);
            }
        }

    }

}

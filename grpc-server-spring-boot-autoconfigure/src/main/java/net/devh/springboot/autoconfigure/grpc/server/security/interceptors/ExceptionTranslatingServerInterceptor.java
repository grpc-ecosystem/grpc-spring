/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.springboot.autoconfigure.grpc.server.security.interceptors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.springboot.autoconfigure.grpc.server.GrpcGlobalServerInterceptor;

/**
 * Server interceptor that translates any {@link AuthenticationException} and {@link AccessDeniedException} to
 * appropriate grpc status responses.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@GrpcGlobalServerInterceptor
public class ExceptionTranslatingServerInterceptor implements ServerInterceptor {

    public static final String UNAUTHENTICATED_DESCRIPTION = "Authentication failed";
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
        call.close(Status.UNAUTHENTICATED.withCause(aex).withDescription(UNAUTHENTICATED_DESCRIPTION), new Metadata());
    }

    /**
     * Close the call with {@link Status#PERMISSION_DENIED}.
     *
     * @param call The call to close.
     * @param aex The exception that was the cause.
     */
    protected void closeCallAccessDenied(final ServerCall<?, ?> call, final AccessDeniedException aex) {
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

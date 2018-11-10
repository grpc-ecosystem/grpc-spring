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

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.GrpcGlobalServerInterceptor;
import net.devh.springboot.autoconfigure.grpc.server.security.authentication.GrpcAuthenticationReader;

/**
 * A server interceptor that tries to {@link GrpcAuthenticationReader read} the credentials from the client and
 * {@link AuthenticationManager#authenticate(Authentication) authenticate} them.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class AuthenticatingServerInterceptor implements ServerInterceptor {

    private final AuthenticationManager authenticationManager;
    private final GrpcAuthenticationReader grpcAuthenticationReader;

    @Autowired
    public AuthenticatingServerInterceptor(final AuthenticationManager authenticationManager,
            final GrpcAuthenticationReader authenticationReader) {
        this.authenticationManager = requireNonNull(authenticationManager, "authenticationManager");
        this.grpcAuthenticationReader = authenticationReader;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call,
            final Metadata headers, final ServerCallHandler<ReqT, RespT> next) {
        Authentication authentication = this.grpcAuthenticationReader.readAuthentication(call, headers);
        if (authentication == null) {
            log.debug("No credentials found: Continuing unauthenticated");
            return next.startCall(call, headers);
        }
        log.debug("Credentials found: Authenticating...");
        authentication = this.authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authentication successful: Continuing as {} ({})", authentication.getName(),
                authentication.getAuthorities());
        return new AuthenticatingServerCallListener<>(next.startCall(call, headers));
    }

    /**
     * A call listener that will clear the authentication context after the call.
     *
     * @param <ReqT> The type of the request.
     */
    private class AuthenticatingServerCallListener<ReqT> extends SimpleForwardingServerCallListener<ReqT> {

        public AuthenticatingServerCallListener(final Listener<ReqT> delegate) {
            super(delegate);
        }

        @Override
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } finally {
                log.debug("Authentication cleared");
                SecurityContextHolder.clearContext();
            }
        }

    }

}

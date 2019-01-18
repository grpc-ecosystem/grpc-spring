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

package net.devh.boot.grpc.server.security.interceptors;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;

/**
 * A server interceptor that tries to {@link GrpcAuthenticationReader read} the credentials from the client and
 * {@link AuthenticationManager#authenticate(Authentication) authenticate} them.
 *
 * <p>
 * <b>Note:</b> This interceptor works similar to
 * {@link Contexts#interceptCall(Context, ServerCall, Metadata, ServerCallHandler)}.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class AuthenticatingServerInterceptor implements ServerInterceptor {

    /**
     * The context key that can be used to retrieve the associated {@link Authentication}.
     */
    public static final Context.Key<Authentication> AUTHENTICATION_CONTEXT_KEY = Context.key("authentication");

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
            try {
                return next.startCall(call, headers);
            } catch (final AccessDeniedException e) {
                throw new BadCredentialsException("No credentials found in the request", e);
            }
        }
        if (authentication.getDetails() == null && authentication instanceof AbstractAuthenticationToken) {
            // Append call attributes to the authentication request.
            // This gives the AuthenticationManager access to information like remote and local address.
            // It can then decide whether it wants to use its own user details or the attributes.
            ((AbstractAuthenticationToken) authentication).setDetails(call.getAttributes());
        }
        log.debug("Credentials found: Authenticating...");
        authentication = this.authenticationManager.authenticate(authentication);

        final Context context = Context.current().withValue(AUTHENTICATION_CONTEXT_KEY, authentication);
        final Context previousContext = context.attach();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authentication successful: Continuing as {} ({})", authentication.getName(),
                authentication.getAuthorities());
        try {
            return new AuthenticatingServerCallListener<>(next.startCall(call, headers), authentication, context);
        } catch (final AccessDeniedException e) {
            if (authentication instanceof AnonymousAuthenticationToken) {
                throw new BadCredentialsException("No credentials found in the request", e);
            } else {
                throw e;
            }
        } finally {
            SecurityContextHolder.clearContext();
            context.detach(previousContext);
            log.debug("startCall - Authentication cleared");
        }
    }

    /**
     * A call listener that will clear the authentication context after the call.
     *
     * @param <ReqT> The type of the request.
     */
    private class AuthenticatingServerCallListener<ReqT> extends SimpleForwardingServerCallListener<ReqT> {

        private final Authentication authentication;
        private final Context context;

        public AuthenticatingServerCallListener(final Listener<ReqT> delegate, final Authentication authentication,
                final Context context) {
            super(delegate);
            this.authentication = authentication;
            this.context = context;
        }

        @Override
        public void onMessage(final ReqT message) {
            final Context previous = this.context.attach();
            try {
                SecurityContextHolder.getContext().setAuthentication(this.authentication);
                log.debug("onMessage - Authentication set");
                super.onMessage(message);
            } finally {
                SecurityContextHolder.clearContext();
                this.context.detach(previous);
                log.debug("onMessage - Authentication cleared");
            }
        }

        @Override
        public void onHalfClose() {
            final Context previous = this.context.attach();
            try {
                SecurityContextHolder.getContext().setAuthentication(this.authentication);
                log.debug("onHalfClose - Authentication set");
                super.onHalfClose();
            } catch (final AccessDeniedException e) {
                if (this.authentication instanceof AnonymousAuthenticationToken) {
                    throw new BadCredentialsException("No credentials found in the request", e);
                } else {
                    throw e;
                }
            } finally {
                SecurityContextHolder.clearContext();
                this.context.detach(previous);
                log.debug("onHalfClose - Authentication cleared");
            }
        }

        @Override
        public void onCancel() {
            final Context previous = this.context.attach();
            try {
                log.debug("onCancel - Authentication set");
                SecurityContextHolder.getContext().setAuthentication(this.authentication);
                super.onCancel();
            } finally {
                log.debug("onCancel - Authentication cleared");
                SecurityContextHolder.clearContext();
                this.context.detach(previous);
            }
        }

        @Override
        public void onComplete() {
            final Context previous = this.context.attach();
            try {
                log.debug("onComplete - Authentication set");
                SecurityContextHolder.getContext().setAuthentication(this.authentication);
                super.onComplete();
            } finally {
                log.debug("onComplete - Authentication cleared");
                SecurityContextHolder.clearContext();
                this.context.detach(previous);
            }
        }

        @Override
        public void onReady() {
            final Context previous = this.context.attach();
            try {
                log.debug("onReady - Authentication set");
                SecurityContextHolder.getContext().setAuthentication(this.authentication);
                super.onReady();
            } finally {
                log.debug("onReady - Authentication cleared");
                SecurityContextHolder.clearContext();
                this.context.detach(previous);
            }
        }

    }

}

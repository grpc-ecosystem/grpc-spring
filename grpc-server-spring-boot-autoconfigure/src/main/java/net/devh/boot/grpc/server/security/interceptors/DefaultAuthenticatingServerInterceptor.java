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

package net.devh.boot.grpc.server.security.interceptors;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;

/**
 * A server interceptor that tries to {@link GrpcAuthenticationReader read} the credentials from the client and
 * {@link AuthenticationManager#authenticate(Authentication) authenticate} them. This interceptor sets the
 * authentication to both grpc's {@link Context} and {@link SecurityContextHolder}.
 *
 * <p>
 * This works similar to the {@code org.springframework.security.web.authentication.AuthenticationFilter}.
 * </p>
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
@Order(InterceptorOrder.ORDER_SECURITY_AUTHENTICATION)
public class DefaultAuthenticatingServerInterceptor implements AuthenticatingServerInterceptor {

    private final AuthenticationManager authenticationManager;
    private final GrpcAuthenticationReader grpcAuthenticationReader;

    /**
     * Creates a new DefaultAuthenticatingServerInterceptor with the given authentication manager and reader.
     *
     * @param authenticationManager The authentication manager used to verify the credentials.
     * @param authenticationReader The authentication reader used to extract the credentials from the call.
     */
    @Autowired
    public DefaultAuthenticatingServerInterceptor(final AuthenticationManager authenticationManager,
            final GrpcAuthenticationReader authenticationReader) {
        this.authenticationManager = requireNonNull(authenticationManager, "authenticationManager");
        this.grpcAuthenticationReader = requireNonNull(authenticationReader, "authenticationReader");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call,
            final Metadata headers, final ServerCallHandler<ReqT, RespT> next) {
        Authentication authentication;
        try {
            authentication = this.grpcAuthenticationReader.readAuthentication(call, headers);
        } catch (final AuthenticationException e) {
            log.debug("Failed to read authentication: {}", e.getMessage());
            throw e;
        }
        if (authentication == null) {
            log.debug("No credentials found: Continuing unauthenticated");
            try {
                return next.startCall(call, headers);
            } catch (final AccessDeniedException e) {
                throw newNoCredentialsException(e);
            }
        }
        if (authentication.getDetails() == null && authentication instanceof AbstractAuthenticationToken) {
            // Append call attributes to the authentication request.
            // This gives the AuthenticationManager access to information like remote and local address.
            // It can then decide whether it wants to use its own user details or the attributes.
            ((AbstractAuthenticationToken) authentication).setDetails(call.getAttributes());
        }
        log.debug("Credentials found: Authenticating '{}'", authentication.getName());
        try {
            authentication = this.authenticationManager.authenticate(authentication);
        } catch (final AuthenticationException e) {
            log.debug("Authentication request failed: {}", e.getMessage());
            onUnsuccessfulAuthentication(call, headers, e);
            throw e;
        }

        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        @SuppressWarnings("deprecation")
        final Context grpcContext = Context.current().withValues(
                SECURITY_CONTEXT_KEY, securityContext,
                AUTHENTICATION_CONTEXT_KEY, authentication);
        final Context previousContext = grpcContext.attach();
        log.debug("Authentication successful: Continuing as {} ({})", authentication.getName(),
                authentication.getAuthorities());
        onSuccessfulAuthentication(call, headers, authentication);
        try {
            return new AuthenticatingServerCallListener<>(next.startCall(call, headers), grpcContext, securityContext);
        } catch (final AccessDeniedException e) {
            if (authentication instanceof AnonymousAuthenticationToken) {
                throw newNoCredentialsException(e);
            } else {
                throw e;
            }
        } finally {
            SecurityContextHolder.clearContext();
            grpcContext.detach(previousContext);
            log.debug("startCall - Authentication cleared");
        }
    }

    /**
     * Hook that will be called on successful authentication. Implementations may only use the call instance in a
     * non-disruptive manor, that is accessing call attributes or the call descriptor. Implementations must not pollute
     * the current thread/context with any call-related state, including authentication, beyond the duration of the
     * method invocation. At the time of calling both the grpc context and the security context have been updated to
     * reflect the state of the authentication and thus don't have to be setup manually.
     *
     * <p>
     * <b>Note:</b> This method is called regardless of whether the authenticated user is authorized or not to perform
     * the requested action.
     * </p>
     *
     * <p>
     * By default, this method does nothing.
     * </p>
     *
     * @param call The call instance to receive response messages.
     * @param headers The headers associated with the call.
     * @param authentication The successful authentication instance.
     */
    protected void onSuccessfulAuthentication(
            final ServerCall<?, ?> call,
            final Metadata headers,
            final Authentication authentication) {
        // Overwrite to add custom behavior.
    }

    /**
     * Hook that will be called on unsuccessful authentication. Implementations must use the call instance only in a
     * non-disruptive manner, i.e. to access call attributes or the call descriptor. Implementations must not close the
     * call and must not pollute the current thread/context with any call-related state, including authentication,
     * beyond the duration of the method invocation.
     *
     * <p>
     * <b>Note:</b> This method is called only if the request contains an authentication but the
     * {@link AuthenticationManager} considers it invalid. This method is not called if an authenticated user is not
     * authorized to perform the requested action.
     * </p>
     *
     * <p>
     * By default, this method does nothing.
     * </p>
     *
     * @param call The call instance to receive response messages.
     * @param headers The headers associated with the call.
     * @param failed The exception related to the unsuccessful authentication.
     */
    protected void onUnsuccessfulAuthentication(
            final ServerCall<?, ?> call,
            final Metadata headers,
            final AuthenticationException failed) {
        // Overwrite to add custom behavior.
    }

    /**
     * Wraps the given {@link AccessDeniedException} in an {@link AuthenticationException} to reflect, that no
     * authentication was originally present in the request.
     *
     * @param denied The caught exception.
     * @return The newly created {@link AuthenticationException}.
     */
    private static AuthenticationException newNoCredentialsException(final AccessDeniedException denied) {
        return new BadCredentialsException("No credentials found in the request", denied);
    }

    /**
     * A call listener that will set the authentication context using {@link SecurityContextHolder} before each
     * invocation and clear it afterwards.
     *
     * @param <ReqT> The type of the request.
     */
    private static class AuthenticatingServerCallListener<ReqT> extends AbstractAuthenticatingServerCallListener<ReqT> {

        private final SecurityContext securityContext;

        /**
         * Creates a new AuthenticatingServerCallListener which will attach the given security context before delegating
         * to the given listener.
         *
         * @param delegate The listener to delegate to.
         * @param grpcContext The context to attach.
         * @param securityContext The security context instance to attach.
         */
        public AuthenticatingServerCallListener(final Listener<ReqT> delegate, final Context grpcContext,
                final SecurityContext securityContext) {
            super(delegate, grpcContext);
            this.securityContext = securityContext;
        }

        @Override
        protected void attachAuthenticationContext() {
            SecurityContextHolder.setContext(this.securityContext);
        }

        @Override
        protected void detachAuthenticationContext() {
            SecurityContextHolder.clearContext();
        }

        @Override
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } catch (final AccessDeniedException e) {
                if (this.securityContext.getAuthentication() instanceof AnonymousAuthenticationToken) {
                    throw newNoCredentialsException(e);
                } else {
                    throw e;
                }
            }
        }

    }

}

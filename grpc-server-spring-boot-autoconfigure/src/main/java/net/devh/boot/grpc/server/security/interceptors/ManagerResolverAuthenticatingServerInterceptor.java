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

import io.grpc.*;
import io.grpc.ServerCall.Listener;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;


import static java.util.Objects.requireNonNull;

/**
 * A server interceptor that tries to {@link GrpcAuthenticationReader read} the credentials from the client and
 * {@link AuthenticationManagerResolver#resolve (Context) grpcServerRequest} them. This interceptor create new {@link DefaultAuthenticatingServerInterceptor} to sets the
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
 * @author Sajad Mehrabi (mehrabisajad@gmail.com)
 */
@Slf4j
@ConditionalOnBean(parameterizedContainer = AuthenticationManagerResolver.class, value = GrpcServerRequest.class)
@GrpcGlobalServerInterceptor
@Order(InterceptorOrder.ORDER_SECURITY_AUTHENTICATION)
public class ManagerResolverAuthenticatingServerInterceptor implements AuthenticatingServerInterceptor {

    private final AuthenticationManagerResolver<GrpcServerRequest> authenticationManagerResolver;
    private final GrpcAuthenticationReader grpcAuthenticationReader;

    /**
     * Creates a new ManagerResolverAuthenticatingServerInterceptor with the given authentication manager resolver and reader.
     *
     * @param authenticationManagerResolver The authentication manager resolver used to verify the credentials.
     * @param authenticationReader The authentication reader used to extract the credentials from the call.
     */
    @Autowired
    public ManagerResolverAuthenticatingServerInterceptor(final AuthenticationManagerResolver<GrpcServerRequest> authenticationManagerResolver,
                                                          final GrpcAuthenticationReader authenticationReader) {
        this.authenticationManagerResolver = requireNonNull(authenticationManagerResolver, "authenticationManagerResolver");
        this.grpcAuthenticationReader = requireNonNull(authenticationReader, "authenticationReader");
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call,
            final Metadata headers, final ServerCallHandler<ReqT, RespT> next) {

        GrpcServerRequest grpcServerRequest = new GrpcServerRequest(call, headers);
        AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(grpcServerRequest);

        if (authenticationManager == null) {
            log.debug("No authenticationManager found: Continuing unauthenticated");
            try {
                return next.startCall(call, headers);
            } catch (final AccessDeniedException e) {
                throw newNoAuthenticationManagerException(e);
            }
        }
        DefaultAuthenticatingServerInterceptor authenticatingServerInterceptor = new DefaultAuthenticatingServerInterceptor(authenticationManager, this.grpcAuthenticationReader);
        return authenticatingServerInterceptor.interceptCall(call, headers, next);

    }


    /**
     * Wraps the given {@link AccessDeniedException} in an {@link AuthenticationException} to reflect, that no
     * authentication was originally present in the request.
     *
     * @param denied The caught exception.
     * @return The newly created {@link AuthenticationException}.
     */
    private static AuthenticationException newNoAuthenticationManagerException(final AccessDeniedException denied) {
        return new BadCredentialsException("No credentials found in the request", denied);
    }

}

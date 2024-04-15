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

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.grpc.*;
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
public class DefaultAuthenticatingServerInterceptor extends AbstractAuthenticatingServerInterceptor {

    private final AuthenticationManager authenticationManager;

    /**
     * Creates a new DefaultAuthenticatingServerInterceptor with the given authentication manager and reader.
     *
     * @param authenticationManager The authentication manager used to verify the credentials.
     * @param authenticationReader The authentication reader used to extract the credentials from the call.
     */
    public DefaultAuthenticatingServerInterceptor(final AuthenticationManager authenticationManager,
            final GrpcAuthenticationReader authenticationReader) {
        super(authenticationReader);
        this.authenticationManager = requireNonNull(authenticationManager, "authenticationManager");
    }

    @Override
    public AuthenticationManager getAuthenticationManager(
            final ServerCall<?, ?> call,
            final Metadata headers) {
        return authenticationManager;
    }
}

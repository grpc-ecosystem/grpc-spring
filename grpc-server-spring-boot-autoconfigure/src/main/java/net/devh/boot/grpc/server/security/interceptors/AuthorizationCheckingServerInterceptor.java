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

import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.core.AuthenticationException;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;

/**
 * A server interceptor that will check the security context whether it has permission to access the grpc method. This
 * interceptor uses a {@link GrpcSecurityMetadataSource} to obtain the information how the called method is protected
 * and uses an {@link AccessDecisionManager} to evaluate that information. This interceptor isn't needed if you use
 * spring's security annotations, but can be used additionally. An example use case of using both would be requiring all
 * users to be authenticated, while using the annotations to require further permissions.
 *
 * <p>
 * <b>Note:</b> If you use spring's security annotations, the you have to use
 * {@code @EnableGlobalMethodSecurity(proxyTargetClass = true, ...)}
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
@Slf4j
@GrpcGlobalServerInterceptor
@Order(InterceptorOrder.ORDER_SECURITY_AUTHORISATION)
public class AuthorizationCheckingServerInterceptor extends AbstractSecurityInterceptor implements ServerInterceptor {

    private final GrpcSecurityMetadataSource securityMetadataSource;

    /**
     * Creates a new AuthorizationCheckingServerInterceptor with the given {@link AccessDecisionManager} and
     * {@link GrpcSecurityMetadataSource}.
     *
     * @param accessDecisionManager The access decision manager to use.
     * @param securityMetadataSource The security metadata source to use.
     */
    public AuthorizationCheckingServerInterceptor(final AccessDecisionManager accessDecisionManager,
            final GrpcSecurityMetadataSource securityMetadataSource) {
        setAccessDecisionManager(requireNonNull(accessDecisionManager, "accessDecisionManager"));
        this.securityMetadataSource = requireNonNull(securityMetadataSource, "securityMetadataSource");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            final ServerCall<ReqT, RespT> call,
            final Metadata headers,
            final ServerCallHandler<ReqT, RespT> next) {

        final InterceptorStatusToken token;
        try {
            token = beforeInvocation(call);
        } catch (final AuthenticationException | AccessDeniedException e) {
            log.debug("Access denied");
            throw e;
        }
        log.debug("Access granted");
        final Listener<ReqT> result;
        try {
            result = next.startCall(call, headers);
        } finally {
            finallyInvocation(token);
        }
        return (Listener<ReqT>) afterInvocation(token, result);
    }

    @Override
    public Class<?> getSecureObjectClass() {
        return ServerCall.class;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.securityMetadataSource;
    }

}

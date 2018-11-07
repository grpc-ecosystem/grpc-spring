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

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.springboot.autoconfigure.grpc.server.GrpcGlobalServerInterceptor;
import net.devh.springboot.autoconfigure.grpc.server.security.check.GrpcSecurityMetadataSource;

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
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@GrpcGlobalServerInterceptor
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
    public <ReqT, RespT> Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, final Metadata headers,
            final ServerCallHandler<ReqT, RespT> next) {
        final MethodDescriptor<ReqT, RespT> methodDescriptor = call.getMethodDescriptor();
        final InterceptorStatusToken token = beforeInvocation(methodDescriptor);
        final Listener<ReqT> result;
        try {
            result = next.startCall(call, headers);
        } finally {
            finallyInvocation(token);
        }
        // TODO: Call that here or in onHalfClose?
        return (Listener<ReqT>) afterInvocation(token, result);
    }

    @Override
    public Class<?> getSecureObjectClass() {
        return MethodDescriptor.class;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.securityMetadataSource;
    }

}

/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.WebSecurityEnablerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;

import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.interceptors.AuthenticatingServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.AuthorizationCheckingServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.DefaultAuthenticatingServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.ExceptionTranslatingServerInterceptor;

/**
 * Auto configuration class with the required beans for the spring-security configuration of the grpc server.
 *
 * <p>
 * To enable security add both an {@link AuthenticationManager} and a {@link GrpcAuthenticationReader} to the
 * application context. The authentication reader obtains the credentials from the requests which then will be validated
 * by the authentication manager. After that, you can decide how you want to secure your application. Currently these
 * options are available:
 * </p>
 *
 * <ul>
 * <li>Use Spring Security's annotations. This requires
 * {@code @EnableGlobalMethodSecurity(proxyTargetClass = true, ...)}.</li>
 * <li>Having both an {@link AccessDecisionManager} and a {@link GrpcSecurityMetadataSource} in the application context.
 * </ul>
 *
 * <p>
 * <b>Note:</b> The order of the beans is important! First the exception translating interceptor, then the
 * authenticating interceptor and finally the authorization checking interceptor. That is necessary because they are
 * executed in the same order as their order.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(AuthenticationManager.class)
@AutoConfigureAfter(WebSecurityEnablerConfiguration.class)
public class GrpcServerSecurityAutoConfiguration {

    /**
     * The interceptor for handling security related exceptions such as {@link AuthenticationException} and
     * {@link AccessDeniedException}.
     *
     * @return The exceptionTranslatingServerInterceptor bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public ExceptionTranslatingServerInterceptor exceptionTranslatingServerInterceptor() {
        return new ExceptionTranslatingServerInterceptor();
    }

    /**
     * The security interceptor that handles the authentication of requests.
     *
     * @param authenticationManager The authentication manager used to verify the credentials.
     * @param authenticationReader The authentication reader used to extract the credentials from the call.
     * @return The authenticatingServerInterceptor bean.
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticatingServerInterceptor.class)
    public DefaultAuthenticatingServerInterceptor authenticatingServerInterceptor(
            final AuthenticationManager authenticationManager,
            final GrpcAuthenticationReader authenticationReader) {
        return new DefaultAuthenticatingServerInterceptor(authenticationManager, authenticationReader);
    }

    /**
     * The security interceptor that handles the authorization of requests.
     *
     * @param accessDecisionManager The access decision manager used to check the requesting user.
     * @param securityMetadataSource The source for the security metadata (access constraints).
     * @return The authorizationCheckingServerInterceptor bean.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({AccessDecisionManager.class, GrpcSecurityMetadataSource.class})
    public AuthorizationCheckingServerInterceptor authorizationCheckingServerInterceptor(
            final AccessDecisionManager accessDecisionManager,
            final GrpcSecurityMetadataSource securityMetadataSource) {
        return new AuthorizationCheckingServerInterceptor(accessDecisionManager, securityMetadataSource);
    }

}

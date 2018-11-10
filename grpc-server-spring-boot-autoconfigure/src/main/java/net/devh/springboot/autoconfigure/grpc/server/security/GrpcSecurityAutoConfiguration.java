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

package net.devh.springboot.autoconfigure.grpc.server.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;

import net.devh.springboot.autoconfigure.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.springboot.autoconfigure.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.springboot.autoconfigure.grpc.server.security.interceptors.AuthenticatingServerInterceptor;
import net.devh.springboot.autoconfigure.grpc.server.security.interceptors.AuthorizationCheckingServerInterceptor;
import net.devh.springboot.autoconfigure.grpc.server.security.interceptors.ExceptionTranslatingServerInterceptor;

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
 * <b>Note:</b> The order of the beans is important! First the authorization checking interceptor, then the
 * authenticating interceptor and finally the exception translating interceptor. That is necessary because they are
 * executed in the reverse order of being declared.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration
@ConditionalOnBean(AuthenticationManager.class)
public class GrpcSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({AccessDecisionManager.class, GrpcSecurityMetadataSource.class})
    public AuthorizationCheckingServerInterceptor authorizationCheckingServerInterceptor(
            final AccessDecisionManager accessDecisionManager,
            final GrpcSecurityMetadataSource securityMetadataSource) {
        return new AuthorizationCheckingServerInterceptor(accessDecisionManager, securityMetadataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticatingServerInterceptor authenticatingServerInterceptor(
            final AuthenticationManager authenticationManager,
            final GrpcAuthenticationReader authenticationReader) {
        return new AuthenticatingServerInterceptor(authenticationManager, authenticationReader);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExceptionTranslatingServerInterceptor exceptionTranslatingServerInterceptor() {
        return new ExceptionTranslatingServerInterceptor();
    }

}

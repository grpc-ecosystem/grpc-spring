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

package net.devh.boot.grpc.server.security.authentication;

import static net.devh.boot.grpc.common.security.SecurityConstants.AUTHORIZATION_HEADER;
import static net.devh.boot.grpc.common.security.SecurityConstants.BEARER_AUTH_PREFIX;

import java.util.function.Function;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring-Security has several token-based {@link AuthenticationProvider} implementations (e.g. in
 * spring-security-web/oauth2 or spring-security-oauth2-resource-server), so you need to provide a {@link Function} that
 * wraps the extracted token in a {@link Authentication} object supported by your AuthenticationProvider.
 *
 * @author Gregor Eeckels (gregor.eeckels@gmail.com)
 */
@Slf4j
public class BearerAuthenticationReader implements GrpcAuthenticationReader {

    private static final String PREFIX = BEARER_AUTH_PREFIX.toLowerCase();
    private static final int PREFIX_LENGTH = PREFIX.length();

    private Function<String, Authentication> tokenWrapper;

    /**
     * Creates a new BearerAuthenticationReader with the given wrapper function.
     * <p>
     * <b>Example-Usage:</b>
     * </p>
     *
     * For spring-security-web:
     *
     * <pre>
     * <code>new BearerAuthenticationReader(token -&gt; new PreAuthenticatedAuthenticationToken(token, null))</code>
     * </pre>
     *
     * For spring-security-oauth2-resource-server:
     *
     * <pre>
     * <code>new BearerAuthenticationReader(token -&gt; new BearerTokenAuthenticationToken(token))</code>
     * </pre>
     * 
     * @param tokenWrapper The function used to convert the token (without bearer prefix) into an {@link Authentication}
     *        object.
     */
    public BearerAuthenticationReader(Function<String, Authentication> tokenWrapper) {
        Assert.notNull(tokenWrapper, "tokenWrapper cannot be null");
        this.tokenWrapper = tokenWrapper;
    }

    @Override
    public Authentication readAuthentication(final ServerCall<?, ?> call, final Metadata headers) {
        final String header = headers.get(AUTHORIZATION_HEADER);

        if (header == null || !header.toLowerCase().startsWith(PREFIX)) {
            log.debug("No bearer auth header found");
            return null;
        }

        // Cut away the "bearer " prefix
        final String accessToken = header.substring(PREFIX_LENGTH);

        // Not authenticated yet, token needs to be processed
        return tokenWrapper.apply(accessToken);
    }
}

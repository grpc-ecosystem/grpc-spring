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

    private final Function<String, Authentication> tokenWrapper;

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

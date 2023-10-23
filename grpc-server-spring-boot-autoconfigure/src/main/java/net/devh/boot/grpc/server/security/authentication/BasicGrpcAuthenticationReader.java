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

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.devh.boot.grpc.common.security.SecurityConstants.AUTHORIZATION_HEADER;
import static net.devh.boot.grpc.common.security.SecurityConstants.BASIC_AUTH_PREFIX;

import java.util.Base64;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads {@link UsernamePasswordAuthenticationToken basic auth credentials} from the request.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class BasicGrpcAuthenticationReader implements GrpcAuthenticationReader {

    private static final String PREFIX = BASIC_AUTH_PREFIX.toLowerCase();
    private static final int PREFIX_LENGTH = PREFIX.length();

    @Override
    public Authentication readAuthentication(final ServerCall<?, ?> call, final Metadata headers)
            throws AuthenticationException {
        final String header = headers.get(AUTHORIZATION_HEADER);
        if (header == null || !header.toLowerCase().startsWith(PREFIX)) {
            log.debug("No basic auth header found");
            return null;
        }
        final String[] decoded = extractAndDecodeHeader(header);
        return new UsernamePasswordAuthenticationToken(decoded[0], decoded[1]);
    }

    /**
     * Decodes the header into a username and password.
     *
     * @param header The authorization header.
     * @return The decoded username and password.
     * @throws BadCredentialsException If the Basic header is not valid Base64 or is missing the {@code ':'} separator.
     * @see <a href=
     *      "https://github.com/spring-projects/spring-security/blob/master/web/src/main/java/org/springframework/security/web/authentication/www/BasicAuthenticationFilter.java">BasicAuthenticationFilter</a>
     */
    private String[] extractAndDecodeHeader(final String header) {

        final byte[] base64Token = header.substring(PREFIX_LENGTH).getBytes(UTF_8);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (final IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token", e);
        }

        final String token = new String(decoded, UTF_8);

        final int delim = token.indexOf(':');

        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return new String[] {token.substring(0, delim), token.substring(delim + 1)};
    }

}

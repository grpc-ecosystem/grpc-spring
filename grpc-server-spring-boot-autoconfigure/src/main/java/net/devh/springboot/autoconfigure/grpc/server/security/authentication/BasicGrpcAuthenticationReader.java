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

package net.devh.springboot.autoconfigure.grpc.server.security.authentication;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    private static final String PREFIX = "basic ";
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

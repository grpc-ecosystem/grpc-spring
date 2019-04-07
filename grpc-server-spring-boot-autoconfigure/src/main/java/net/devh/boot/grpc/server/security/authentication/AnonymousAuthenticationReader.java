/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

import static java.util.Objects.requireNonNull;

import java.util.Collection;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import lombok.extern.slf4j.Slf4j;

/**
 * The AnonymousAuthenticationReader allows users without credentials to get an anonymous identity.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class AnonymousAuthenticationReader implements GrpcAuthenticationReader {

    private final String key;
    private final Object principal;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates a new AnonymousAuthenticationReader with the given key and {@code "anonymousUser"} as principal with the
     * {@code ROLE_ANONYMOUS}.
     *
     * @param key The key to used to identify tokens that were created by this instance.
     */
    public AnonymousAuthenticationReader(final String key) {
        this(key, "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }

    /**
     * Creates a new AnonymousAuthenticationReader with the given key,principal and authorities.
     *
     * @param key The key to used to identify tokens that were created by this instance.
     * @param principal The principal which will be used to represent anonymous users.
     * @param authorities The authority list for anonymous users.
     */
    public AnonymousAuthenticationReader(final String key, final Object principal,
            final Collection<? extends GrantedAuthority> authorities) {
        this.key = requireNonNull(key, "key");
        this.principal = requireNonNull(principal, "principal");
        this.authorities = requireNonNull(authorities, "authorities");
    }

    @Override
    public Authentication readAuthentication(final ServerCall<?, ?> call, final Metadata headers) {
        log.debug("Continue with anonymous auth");
        return new AnonymousAuthenticationToken(this.key, this.principal, this.authorities);
    }

}

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

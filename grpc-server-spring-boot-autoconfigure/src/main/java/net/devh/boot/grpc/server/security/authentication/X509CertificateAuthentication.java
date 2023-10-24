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

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * An authentication object that was created for a {@link X509Certificate}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class X509CertificateAuthentication extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -5783300616514990238L;

    private final Object principal;
    private X509Certificate certificate;

    /**
     * Creates a new X509CertificateAuthentication that will use the given certificate. Any code can safely use this
     * constructor to create an {@link Authentication}, because the {@link #isAuthenticated()} will return
     * {@code false}.
     *
     * @param certificate The certificate to create the authentication from.
     */
    public X509CertificateAuthentication(final X509Certificate certificate) {
        super(Collections.emptyList());
        requireNonNull(certificate, "certificate");
        this.principal = certificate.getSubjectX500Principal();
        this.certificate = certificate;
        setAuthenticated(false);
    }

    /**
     * Creates a new X509CertificateAuthentication that was authenticated using the given certificate. This constructor
     * should only be used by {@link AuthenticationManager}s or {@link AuthenticationProvider}s. The resulting
     * authentication is trusted ({@link #isAuthenticated()} returns true) and has the given authorities.
     *
     * @param principal The authenticated principal.
     * @param certificate The certificate that was used to authenticate the principal.
     * @param authorities The authorities of the principal.
     */
    public X509CertificateAuthentication(final Object principal, final X509Certificate certificate,
            final Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = requireNonNull(principal, "principal");
        this.certificate = requireNonNull(certificate, "certificate");
        super.setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public X509Certificate getCredentials() {
        return this.certificate;
    }

    @Override
    public void eraseCredentials() {
        this.certificate = null;
        super.eraseCredentials();
    }

    @Override
    public void setAuthenticated(final boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

}

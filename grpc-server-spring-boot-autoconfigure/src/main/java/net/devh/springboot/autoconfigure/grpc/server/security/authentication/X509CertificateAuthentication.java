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

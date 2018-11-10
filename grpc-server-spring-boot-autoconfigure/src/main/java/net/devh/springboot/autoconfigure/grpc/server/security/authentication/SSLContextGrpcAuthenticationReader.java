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

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.annotation.Nullable;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.springframework.security.core.Authentication;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import lombok.extern.slf4j.Slf4j;

/**
 * An {@link GrpcAuthenticationReader} that will try to use the peer certificates to extract the client
 * {@link Authentication}. Currently this class only supports {@link X509Certificate}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class SSLContextGrpcAuthenticationReader implements GrpcAuthenticationReader {

    @Override
    public Authentication readAuthentication(final ServerCall<?, ?> call, final Metadata metadata) {
        final SSLSession sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
        if (sslSession == null) {
            log.trace("Peer not verified via SSL");
            return null;
        }
        Certificate[] certs;
        try {
            certs = sslSession.getPeerCertificates();
        } catch (final SSLPeerUnverifiedException e) {
            log.trace("Peer not verified via certificate", e);
            return null;
        }
        return fromCertificate(certs[certs.length - 1]);
    }

    /**
     * Tries to prepare an {@link Authentication} using the given certificate.
     *
     * @param cert The certificate to use.
     * @return The authentication instance created with the certificate or null if the certificate type is unsupported.
     */
    @Nullable
    protected Authentication fromCertificate(final Certificate cert) {
        if (cert instanceof X509Certificate) {
            log.debug("Found X509 certificate");
            return new X509CertificateAuthentication((X509Certificate) cert);
        } else {
            log.debug("Unsupported certificate type: {}", cert.getType());
            return null;
        }
    }

}

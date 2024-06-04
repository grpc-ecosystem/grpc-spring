/*
 * Copyright (c) 2016-2024 The gRPC-Spring Authors
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

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNamesBuilder;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CertificateHelper {

    static final Provider PROVIDER = new BouncyCastleProvider();
    static {
        Security.addProvider(PROVIDER);
    }

    final SecureRandom secureRandom = new SecureRandom();

    CertificateAndKeys rootCertificate(String subject)
            throws NoSuchAlgorithmException, CertIOException, CertificateException, OperatorCreationException {
        var keyPair = keyPair();
        var subjectName = new X500Name(subject);
        var certBuilder = buildCertificate(subjectName, subjectName, keyPair.getPublic());
        var extensionUtils = new JcaX509ExtensionUtils();

        var subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(keyPair.getPublic());
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);

        var keyUsage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign);
        certBuilder.addExtension(Extension.keyUsage, true, keyUsage);

        var constraints = new BasicConstraints(0);
        certBuilder.addExtension(Extension.basicConstraints, true, constraints);

        var certificate = signCertificate(certBuilder, keyPair.getPrivate());
        return new CertificateAndKeys(certificate, keyPair);
    }

    CertificateAndKeys intermediateCertificate(String subject, CertificateAndKeys issuer)
            throws NoSuchAlgorithmException, CertIOException, CertificateException, OperatorCreationException {
        var keyPair = keyPair();
        var issuerCertificate = issuer.certificate();
        var certBuilder = buildCertificate(new JcaX509CertificateHolder(issuerCertificate).getSubject(),
                new X500Name(subject), keyPair.getPublic());
        var extensionUtils = new JcaX509ExtensionUtils();

        var subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(keyPair.getPublic());
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);

        var authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(issuerCertificate);
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier);

        var keyUsage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign);
        certBuilder.addExtension(Extension.keyUsage, true, keyUsage);

        var constraints = new BasicConstraints(0);
        certBuilder.addExtension(Extension.basicConstraints, true, constraints);

        var certificate = signCertificate(certBuilder, issuer.keyPair().getPrivate());
        return new CertificateAndKeys(certificate, keyPair);
    }

    CertificateAndKeys leafCertificate(String subject, CertificateAndKeys issuer)
            throws NoSuchAlgorithmException, CertIOException, CertificateException, OperatorCreationException {
        var keyPair = keyPair();
        var issuerCertificate = issuer.certificate();
        var certBuilder = buildCertificate(new JcaX509CertificateHolder(issuerCertificate).getSubject(),
                new X500Name(subject), keyPair.getPublic());
        var extensionUtils = new JcaX509ExtensionUtils();

        var subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(keyPair.getPublic());
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);

        var authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(issuerCertificate);
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier);

        var keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment);
        certBuilder.addExtension(Extension.keyUsage, true, keyUsage);

        var extendedKeyUsage =
                new ExtendedKeyUsage(new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth});
        certBuilder.addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage);

        certBuilder.addExtension(Extension.subjectAlternativeName, false, new GeneralNamesBuilder()
                .addName(new GeneralName(GeneralName.dNSName, "localhost"))
                .addName(new GeneralName(GeneralName.iPAddress, "127.0.0.1"))
                .build());

        var certificate = signCertificate(certBuilder, issuer.keyPair().getPrivate());
        return new CertificateAndKeys(certificate, keyPair);
    }

    private X509v3CertificateBuilder buildCertificate(X500Name issuer, X500Name subject, PublicKey publicKey) {
        var publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        var now = ZonedDateTime.now();
        return new X509v3CertificateBuilder(
                issuer,
                new BigInteger(160, secureRandom),
                Date.from(now.toInstant()),
                Date.from(now.plusDays(1).toInstant()),
                subject,
                publicKeyInfo);
    }

    private X509Certificate signCertificate(X509v3CertificateBuilder certificateBuilder, PrivateKey privateKey)
            throws OperatorCreationException, CertificateException {
        var signer = contentSigner(privateKey);
        var certificateHolder = certificateBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider(PROVIDER)
                .getCertificate(certificateHolder);
    }

    private KeyPair keyPair() throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA", PROVIDER);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private ContentSigner contentSigner(PrivateKey privateKey) throws OperatorCreationException {
        return new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider(PROVIDER)
                .build(privateKey);
    }

    record CertificateAndKeys(X509Certificate certificate, KeyPair keyPair) {}

}

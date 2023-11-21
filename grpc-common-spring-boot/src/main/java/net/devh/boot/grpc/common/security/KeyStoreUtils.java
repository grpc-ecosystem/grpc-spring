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

package net.devh.boot.grpc.common.security;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableMap;

/**
 * Helper class to create {@link KeyStore} related components.
 */
public final class KeyStoreUtils {

    /**
     * Automatically detects the keystore format.
     *
     * @see #detectFormat(String, String)
     */
    public static final String FORMAT_AUTODETECT = "AUTODETECT";
    /**
     * The fallback keystore format to use.
     */
    public static final String FORMAT_FALLBACK = KeyStore.getDefaultType();
    private static final Map<String, String> FORMAT_MAPPING = ImmutableMap.<String, String>builder()
            .put("jks", "JKS")
            .put("p12", "PKCS12")
            .put("pfx", "PKCS12")
            .build();

    /**
     * Detects the keystore format from the given keystore file name.
     *
     * @param format The format to base the decision on.
     * @param name The filename of the keystore.
     * @return The detected keystore format. If unknown defaults to the systems default.
     */
    public static String detectFormat(final String format, final String name) {
        if (FORMAT_AUTODETECT.equals(format)) {
            if (name == null) {
                return FORMAT_FALLBACK;
            }
            final int index = name.lastIndexOf('.');
            if (index == -1) {
                return FORMAT_FALLBACK;
            } else {
                final String ending = name.substring(index + 1).toLowerCase();
                return FORMAT_MAPPING.getOrDefault(ending, FORMAT_FALLBACK);
            }
        } else {
            return format;
        }
    }

    /**
     * Creates a new {@link KeyManagerFactory} from the given {@link KeyStore} {@link Resource}.
     *
     * @param keyStoreFormat The format of the keystore.
     * @param keyStore The resource containing the keystore.
     * @param keyStorePassword The password for the keystore. May be empty or null.
     * @return The newly created KeyManagerFactory.
     * @throws KeyStoreException If the keystore format isn't supported.
     * @throws IOException If there is an I/O or format problem with the keystore data, if a password is required but
     *         not given, or if the given password was incorrect. If the error is due to a wrong password, the
     *         {@link Throwable#getCause cause} of the {@code IOException} should be an
     *         {@code UnrecoverableKeyException}
     * @throws NoSuchAlgorithmException If the algorithm used to check the integrity of the keystore cannot be found
     * @throws CertificateException If any of the certificates in the keystore could not be loaded
     * @throws UnrecoverableKeyException If the key cannot be recovered(e.g. the given password is wrong).
     */
    public static KeyManagerFactory loadKeyManagerFactory(
            final String keyStoreFormat,
            final Resource keyStore,
            final String keyStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException {

        final KeyStore ks = loadKeyStore(keyStoreFormat, keyStore, keyStorePassword);
        final String keyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyManagerAlgorithm);
        keyManagerFactory.init(ks, keystorePassword(keyStorePassword));
        return keyManagerFactory;
    }

    /**
     * Creates a new {@link TrustManagerFactory} from the given {@link KeyStore} {@link Resource}.
     *
     * @param trustStoreFormat The format of the truststore.
     * @param trustStore The resource containing the truststore.
     * @param trustStorePassword The password for the truststore. May be empty or null.
     * @return The newly created KeyManagerFactory.
     * @throws KeyStoreException If the keystore format isn't supported.
     * @throws IOException If there is an I/O or format problem with the keystore data, if a password is required but
     *         not given, or if the given password was incorrect. If the error is due to a wrong password, the
     *         {@link Throwable#getCause cause} of the {@code IOException} should be an
     *         {@code UnrecoverableKeyException}
     * @throws NoSuchAlgorithmException If the algorithm used to check the integrity of the keystore cannot be found
     * @throws CertificateException If any of the certificates in the keystore could not be loaded
     */
    public static TrustManagerFactory loadTrustManagerFactory(
            final String trustStoreFormat,
            final Resource trustStore,
            final String trustStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        final KeyStore ks = loadKeyStore(trustStoreFormat, trustStore, trustStorePassword);
        final String trustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerAlgorithm);
        trustManagerFactory.init(ks);
        return trustManagerFactory;
    }

    /**
     * Loads the {@link KeyStore} from the given {@link Resource}.
     *
     * @param keyStoreFormat The format of the keystore.
     * @param keyStore The resource containing the keystore.
     * @param keyStorePassword The password for the keystore. May be empty or null.
     * @return The newly created KeyStore.
     * @throws KeyStoreException If the keystore format isn't supported.
     * @throws IOException If there is an I/O or format problem with the keystore data, if a password is required but
     *         not given, or if the given password was incorrect. If the error is due to a wrong password, the
     *         {@link Throwable#getCause cause} of the {@code IOException} should be an
     *         {@code UnrecoverableKeyException}
     * @throws NoSuchAlgorithmException If the algorithm used to check the integrity of the keystore cannot be found
     * @throws CertificateException If any of the certificates in the keystore could not be loaded
     */
    private static KeyStore loadKeyStore(
            final String keyStoreFormat,
            final Resource keyStore,
            final String keyStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        requireNonNull(keyStoreFormat, "keyStoreFormat");
        requireNonNull(keyStore, "keyStore");
        final String detectedFormat = detectFormat(keyStoreFormat, keyStore.getFilename());
        final KeyStore ks = KeyStore.getInstance(detectedFormat);
        try (InputStream stream = keyStore.getInputStream()) {
            ks.load(stream, keystorePassword(keyStorePassword));
        }
        return ks;
    }

    private static char[] keystorePassword(final String password) {
        if (password == null) {
            return new char[0];
        } else {
            return password.toCharArray();
        }
    }


    private KeyStoreUtils() {}

}

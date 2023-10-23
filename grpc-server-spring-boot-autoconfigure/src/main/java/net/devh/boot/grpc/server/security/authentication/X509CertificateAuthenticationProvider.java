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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * An {@link AuthenticationProvider} for {@link X509Certificate}s. This provider only supports
 * {@link X509CertificateAuthentication}s.
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
@Slf4j
public class X509CertificateAuthenticationProvider implements AuthenticationProvider {

    /**
     * The uses the name of the principal way to extract the username from an {@link Authentication}.
     */
    public static final Function<Authentication, String> PRINCIPAL_USERNAME_EXTRACTOR = Authentication::getName;

    /**
     * The default way to extract the username from an {@link Authentication} by using the CN.
     */
    public static final Function<X509CertificateAuthentication, String> CN_USERNAME_EXTRACTOR =
            patternExtractor("CN", PRINCIPAL_USERNAME_EXTRACTOR);

    /**
     * A fallback that will fail to extract the username and will return null. The null will later be converted to a
     * {@link UsernameNotFoundException}.
     */
    public static final Function<Authentication, String> FAIL_FALLBACK = authentication -> null;

    /**
     * Creates a new case-insensitive pattern extractor with the given pattern.
     *
     * @param key The case insensitive key to use (Example: 'CN').
     * @param fallback The fallback function to use if the key was not present in the subject.
     * @return The newly created extractor.
     */
    public static Function<X509CertificateAuthentication, String> patternExtractor(final String key,
            final Function<? super X509CertificateAuthentication, String> fallback) {
        requireNonNull(key, "key");
        requireNonNull(fallback, "fallback");
        final Pattern pattern = Pattern.compile(key + "=(.+?)(?:,|$)", Pattern.CASE_INSENSITIVE);
        return authentication -> {
            final Object principal = authentication.getPrincipal();
            if (principal instanceof X500Principal) {
                final X500Principal x500Principal = (X500Principal) principal;
                final Matcher matcher = pattern.matcher(x500Principal.getName());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            return fallback.apply(authentication);
        };
    }

    private final Function<? super X509CertificateAuthentication, String> usernameExtractor;
    private final UserDetailsService userDetailsService;

    /**
     * Creates a new X509CertificateAuthenticationProvider, which uses the {@link #CN_USERNAME_EXTRACTOR default way
     * (via CN)} to extract the username and uses the given {@link UserDetailsService} to lookup the user.
     *
     * @param userDetailsService The user details service to use.
     */
    public X509CertificateAuthenticationProvider(final UserDetailsService userDetailsService) {
        this(CN_USERNAME_EXTRACTOR, userDetailsService);
    }

    /**
     * Creates a new X509CertificateAuthenticationProvider, which uses the given {@link Function} to extract the
     * username and uses the given {@link UserDetailsService} to lookup the user.
     *
     * @param usernameExtractor The username extractor to use. The function should return null, if the username is
     *        missing.
     * @param userDetailsService The user details service to use.
     */
    public X509CertificateAuthenticationProvider(
            final Function<? super X509CertificateAuthentication, String> usernameExtractor,
            final UserDetailsService userDetailsService) {
        this.usernameExtractor = requireNonNull(usernameExtractor, "usernameExtractor");
        this.userDetailsService = requireNonNull(userDetailsService, "userDetailsService");
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof X509CertificateAuthentication)) {
            throw new IllegalArgumentException("Unsupported authentication type: " + authentication.getClass().getName()
                    + ". Only X509CertificateAuthentication is supported!");
        }

        final X509CertificateAuthentication auth = (X509CertificateAuthentication) authentication;
        final String username = this.usernameExtractor.apply(auth);
        if (username == null) {
            log.debug("Could not find username");
            throw new UsernameNotFoundException("No username provided");
        }

        final UserDetails user = this.userDetailsService.loadUserByUsername(username);
        if (user == null) {
            log.debug("Could not find user '{}'", username);
            throw new UsernameNotFoundException("Unknown username: " + username);
        }
        log.debug("Authenticated as '{}'", username);
        return new X509CertificateAuthentication(user, auth.getCredentials(), user.getAuthorities());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return X509CertificateAuthentication.class.isAssignableFrom(authentication);
    }

}

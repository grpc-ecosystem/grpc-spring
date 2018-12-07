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

package net.devh.boot.grpc.examples.security.server;

import java.text.ParseException;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * @author Gregor Eeckels (gregor.eeckels@gmail.com)
 *
 */
public class OIDCAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        if (!supports(authentication.getClass())) {
            throw new ProviderNotFoundException("Only PreAuthenticatedAuthenticationToken supported");
        }

        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;

            try {
                // Parse JWT
                JWT parsedToken = JWTParser.parse((String) token.getPrincipal());

                // Create OidcIdToken object
                OidcIdToken oidcToken = new OidcIdToken((String) token.getPrincipal(),
                        parsedToken.getJWTClaimsSet().getIssueTime().toInstant(),
                        parsedToken.getJWTClaimsSet().getExpirationTime().toInstant(),
                        parsedToken.getJWTClaimsSet().getClaims());

                // Create AuthoritiesList
                HashSet<GrantedAuthority> authorities = new HashSet<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_GREET"));

                // Create OAuth2User
                OAuth2User user = new DefaultOidcUser(authorities, oidcToken);

                return new OAuth2AuthenticationToken(user, authorities, parsedToken.getJWTClaimsSet().getSubject());
            } catch (ParseException e) {
                throw new BadCredentialsException("Invalid JWT token");
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.authentication.AuthenticationProvider#supports (java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

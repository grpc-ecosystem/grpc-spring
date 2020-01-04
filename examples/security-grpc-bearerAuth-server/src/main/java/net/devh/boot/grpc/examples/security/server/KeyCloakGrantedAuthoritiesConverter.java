/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * Granted Authorities Converter for Keycloak Access Tokens
 * 
 * @author Gregor Eeckels (gregor.eeckels@gmail.com
 */
public class KeyCloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String SCOPE_AUTHORITY_PREFIX = "ROLE_";
    private static final Collection<String> WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES =
            Arrays.asList("realm_access");

    /**
     * Extracts the authorities
     * 
     * @param jwt The {@link Jwt} token
     * @return The {@link GrantedAuthority authorities} read from the token scopes
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return getScopes(jwt)
                .stream()
                .map(authority -> SCOPE_AUTHORITY_PREFIX + authority.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets the realm access roles from a {@link Jwt} Keycloak access token. This can also be done for any kind of
     * roles/scopes contained in the token. Adapt this method accordingly.
     * 
     * @param jwt The {@link Jwt} access token
     * @return The realm access roles from the token
     */
    private Collection<String> getScopes(Jwt jwt) {
        Collection<String> result = new ArrayList<>();
        for (String attributeName : WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES) {
            // Retrieve realm_access entry from access token
            JSONObject realm_access = (JSONObject) jwt.getClaims().get(attributeName);
            if (Objects.isNull(realm_access)) {
                return Collections.emptyList();
            }

            // Retrieve roles from realm_access
            JSONArray roles = (JSONArray) realm_access.get("roles");
            if (Objects.isNull(roles)) {
                return Collections.emptyList();
            }

            for (Object role : roles) {
                result.add((String) role);
            }
        }
        return result;
    }

}

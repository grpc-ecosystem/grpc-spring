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

package net.devh.boot.grpc.server.security.check;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import io.grpc.ServerCall;

/**
 * An {@link AccessDecisionVoter} that checks for {@link AccessPredicateConfigAttribute}s.
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
public class AccessPredicateVoter implements AccessDecisionVoter<ServerCall<?, ?>> {

    @Override
    public boolean supports(final ConfigAttribute attribute) {
        return attribute instanceof AccessPredicateConfigAttribute;
    }

    @Override
    public boolean supports(final Class<?> clazz) {
        return ServerCall.class.isAssignableFrom(clazz);
    }

    @Override
    public int vote(final Authentication authentication, final ServerCall<?, ?> serverCall,
            final Collection<ConfigAttribute> attributes) {
        final AccessPredicateConfigAttribute attr = find(attributes);
        if (attr == null) {
            return ACCESS_ABSTAIN;
        }
        final boolean allowed = attr.getAccessPredicate().test(authentication, serverCall);
        return allowed ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    /**
     * Finds the first AccessPredicateConfigAttribute in the given collection.
     *
     * @param attributes The attributes to search in.
     * @return The first found AccessPredicateConfigAttribute or null, if no such elements were found.
     */
    private AccessPredicateConfigAttribute find(final Collection<ConfigAttribute> attributes) {
        for (final ConfigAttribute attribute : attributes) {
            if (attribute instanceof AccessPredicateConfigAttribute) {
                return (AccessPredicateConfigAttribute) attribute;
            }
        }
        return null;
    }

}

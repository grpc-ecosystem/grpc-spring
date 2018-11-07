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

package net.devh.springboot.autoconfigure.grpc.server.security.check;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

/**
 * An {@link AccessDecisionVoter} that checks for {@link AccessPredicateConfigAttribute}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class AccessPredicateVoter implements AccessDecisionVoter<Object> {

    @Override
    public boolean supports(final ConfigAttribute attribute) {
        return attribute instanceof AccessPredicateConfigAttribute;
    }

    @Override
    public boolean supports(final Class<?> clazz) {
        return true;
    }

    @Override
    public int vote(final Authentication authentication, final Object object,
            final Collection<ConfigAttribute> attributes) {
        final AccessPredicateConfigAttribute attr = find(attributes);
        if (attr == null) {
            return ACCESS_ABSTAIN;
        }
        final boolean allowed = attr.getAccessPredicate().test(authentication);
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

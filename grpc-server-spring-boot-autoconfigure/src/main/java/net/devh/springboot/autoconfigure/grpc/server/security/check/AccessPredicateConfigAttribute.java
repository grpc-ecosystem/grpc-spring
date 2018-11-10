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

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.springframework.security.access.ConfigAttribute;

/**
 * A {@link ConfigAttribute} which uses the embedded {@link AccessPredicate} for the decisions.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class AccessPredicateConfigAttribute implements ConfigAttribute {

    private static final long serialVersionUID = 2906954441251029428L;

    private final AccessPredicate accessPredicate;

    /**
     * Creates a new AccessPredicateConfigAttribute with the given {@link AccessPredicate}.
     *
     * @param accessPredicate The access predicate to use.
     */
    public AccessPredicateConfigAttribute(final AccessPredicate accessPredicate) {
        this.accessPredicate = requireNonNull(accessPredicate, "accessPredicate");
    }

    /**
     * Gets the access predicate that belongs to this instance.
     *
     * @return The associated access predicate.
     */
    public AccessPredicate getAccessPredicate() {
        return this.accessPredicate;
    }

    @Override
    public String getAttribute() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.accessPredicate);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AccessPredicateConfigAttribute other = (AccessPredicateConfigAttribute) obj;
        return Objects.equals(this.accessPredicate, other.accessPredicate);
    }

    @Override
    public String toString() {
        return "AccessPredicateConfigAttribute [" + this.accessPredicate + "]";
    }

}

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

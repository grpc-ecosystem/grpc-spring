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

import java.util.function.Predicate;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;

import io.grpc.ServerCall;

/**
 * Helper class that contains some internal constants for {@link AccessPredicate}s.
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
final class AccessPredicates {

    /**
     * A marker constant that indicates that all restrictions should be disabled. This instance should never be
     * executed, mutated or used in mutation. It should only be used in {@code ==} comparisons.
     */
    static final AccessPredicate PERMIT_ALL = new AccessPredicate() {

        /**
         * @deprecated Should never be called
         */
        @Override
        @Deprecated // Should never be called
        public boolean test(final Authentication t, ServerCall<?, ?> serverCall) {
            throw new InternalAuthenticationServiceException(
                    "Tried to execute the 'permit-all' access predicate. The server's security configuration is broken.");
        }

        /**
         * @deprecated Should never be called
         */
        @Override
        @Deprecated // Should never be called
        public AccessPredicate and(final Predicate<? super Authentication> other) {
            throw fail();
        }

        /**
         * @deprecated Should never be called
         */
        @Override
        @Deprecated // Should never be called
        public AccessPredicate or(final Predicate<? super Authentication> other) {
            throw fail();
        }

        /**
         * @deprecated Should never be called
         */
        @Override
        @Deprecated // Should never be called
        public AccessPredicate negate() {
            throw fail();
        }

        private UnsupportedOperationException fail() {
            return new UnsupportedOperationException("Not allowed for 'permit-all' access predicate");
        }

    };

    private AccessPredicates() {}
}

/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.security.check;

import java.util.function.Predicate;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;

/**
 * Helper class that contains some internal constants for {@link AccessPredicate}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
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
        public boolean test(final Authentication t) {
            throw new InternalAuthenticationServiceException(
                    "Tried to execute the 'permit-all' access predicate. The server's security configuration is broken.");
        }

        /**
         * @deprecated Should never be called
         */
        @Override
        @Deprecated // Should never be called
        public AccessPredicate and(final Predicate<? super Authentication> other) {
            throw new UnsupportedOperationException("Not allowed for 'permit-all' access predicate");
        }

        /**
         * @deprecated Should never be called
         */
        @Override
        @Deprecated // Should never be called
        public AccessPredicate or(final Predicate<? super Authentication> other) {
            throw new UnsupportedOperationException("Not allowed for 'permit-all' access predicate");
        }

        /**
         * @deprecated Should never be called
         */
        @Override
        @Deprecated // Should never be called
        public AccessPredicate negate() {
            throw new UnsupportedOperationException("Not allowed for 'permit-all' access predicate");
        }

    };

    private AccessPredicates() {}
}

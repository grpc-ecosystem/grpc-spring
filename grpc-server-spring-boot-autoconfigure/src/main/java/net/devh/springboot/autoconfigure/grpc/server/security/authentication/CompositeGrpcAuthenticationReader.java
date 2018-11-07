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

package net.devh.springboot.autoconfigure.grpc.server.security.authentication;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import io.grpc.Metadata;
import io.grpc.ServerCall;

/**
 * Combines multiple {@link GrpcAuthenticationReader} into a single one. The interceptors will be executed in the same
 * order the are passed to the constructor. The authentication is aborted if a grpc authentication reader throws an
 * exception.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class CompositeGrpcAuthenticationReader implements GrpcAuthenticationReader {

    private final List<GrpcAuthenticationReader> authenticationReaders;

    /**
     * Creates a new CompositeGrpcAuthenticationReader with the given authentication readers.
     *
     * @param authenticationReaders The authentication readers to use.
     */
    public CompositeGrpcAuthenticationReader(final List<GrpcAuthenticationReader> authenticationReaders) {
        this.authenticationReaders = new ArrayList<>(requireNonNull(authenticationReaders, "authenticationReaders"));
    }

    @Override
    public Authentication readAuthentication(final ServerCall<?, ?> call, final Metadata headers)
            throws AuthenticationException {
        for (final GrpcAuthenticationReader authenticationReader : this.authenticationReaders) {
            final Authentication authentication = authenticationReader.readAuthentication(call, headers);
            if (authentication != null) {
                return authentication;
            }
        }
        return null;
    }

}

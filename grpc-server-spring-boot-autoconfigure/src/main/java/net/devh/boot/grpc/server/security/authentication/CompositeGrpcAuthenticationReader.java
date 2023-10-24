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

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import io.grpc.Metadata;
import io.grpc.ServerCall;

/**
 * Combines multiple {@link GrpcAuthenticationReader} into a single one. The readers will be executed in the same order
 * the are passed to the constructor. The authentication is aborted if a grpc authentication reader throws an exception.
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

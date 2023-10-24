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

import javax.annotation.Nullable;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;

/**
 * Reads the authentication data from the given {@link ServerCall} and {@link Metadata}. The returned
 * {@link Authentication} is not yet validated and needs to be passed to an {@link AuthenticationManager}.
 *
 * <p>
 * This is similar to the {@code org.springframework.security.web.authentication.AuthenticationConverter}.
 * </p>
 *
 * <p>
 * <b>Note:</b> The authentication manager needs a corresponding {@link AuthenticationProvider} to actually verify the
 * {@link Authentication}.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@FunctionalInterface
public interface GrpcAuthenticationReader {

    /**
     * Tries to read the {@link Authentication} information from the given call and metadata.
     *
     * <p>
     * <b>Note:</b> Implementations are free to throw an {@link AuthenticationException} if no credentials could be
     * found in the call. If an exception is thrown by an implementation then the authentication attempt should be
     * considered as failed and no subsequent {@link GrpcAuthenticationReader}s should be called. Additionally, the call
     * will fail as {@link Status#UNAUTHENTICATED}. If the call instead returns {@code null}, then the call processing
     * will proceed unauthenticated.
     * </p>
     *
     * @param call The call to get that send the request.
     * @param headers The metadata/headers as sent by the client.
     * @return The authentication object or null if no authentication is present.
     * @throws AuthenticationException If the authentication details are malformed or incomplete and thus the
     *         authentication attempt should be aborted.
     */
    @Nullable
    Authentication readAuthentication(ServerCall<?, ?> call, Metadata headers) throws AuthenticationException;

}

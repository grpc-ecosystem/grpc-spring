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

import javax.annotation.Nullable;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.ServerCall;

/**
 * Reads the authentication data from the given {@link ServerCall} and {@link Metadata}. The returned
 * {@link Authentication} is not yet validated and needs to be passed to an {@link AuthenticationManager}.
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
     * A convenience constant that contains the key for the HTTP Authorization Header.
     */
    Key<String> AUTHORIZATION_HEADER = Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Tries to read the {@link Authentication} information from the given call and metadata.
     *
     * <p>
     * <b>Note:</b> Implementations are free to throw an {@link AuthenticationException} if no credentials could be
     * found in the call. If an exception is thrown by an implementation then the authentication attempt should be
     * considered as failed and no subsequent {@link GrpcAuthenticationReader}s should be called.
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

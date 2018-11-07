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

package net.devh.springboot.autoconfigure.grpc.client.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.stub.MetadataUtils;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;

/**
 * Helper class that can be used to create authenticating {@link ClientInterceptor}.
 *
 * <p>
 * These interceptors can be used like this:
 * </p>
 *
 * <pre>
 * <code>
 * &#64;Bean
 * ClientInterceptor basicAuthInterceptor() {
 *     return basicAuth("foo", "bar");
 * }
 * </code>
 * </pre>
 *
 *
 * <ul>
 * <li>If you only need a single authenticating client interceptor for all clients, then you can register it globally.
 *
 * <pre>
 * <code>&#64;Bean
 * public GlobalClientInterceptorConfigurer basicAuthInterceptorConfigurer() {
 *     return registry -&gt; registry.addClientInterceptors(basicAuthInterceptor());
 * }</code>
 * </pre>
 *
 * </li>
 *
 * <li>If you need different authenticating client interceptors, then you can use the
 * {@link GrpcClient#interceptorNames()} field to add a bean by name.
 *
 * <pre>
 * <code>&#64;GrpcClient(value = "testChannel", interceptorNames = "basicAuthInterceptor")
 * private Channel testChannel;</code>
 * </pre>
 *
 * </li>
 * </ul>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class AuthenticatingClientInterceptors {

    /**
     * A convenience constant that contains the key for the HTTP Authorization Header.
     */
    public static final Key<String> AUTHORIZATION_HEADER = Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Creates a new {@link ClientInterceptor} that adds the given username and passwords as basic auth to all requests.
     * The header will be encoded with {@link StandardCharsets#UTF_8 UTF_8}.
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return The newly created basic auth interceptor.
     * @see #encodeBasicAuth(String, String)
     */
    public static ClientInterceptor basicAuth(final String username, final String password) {
        final Metadata extraHeaders = new Metadata();
        extraHeaders.put(AUTHORIZATION_HEADER, encodeBasicAuth(username, password));
        return MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
    }

    /**
     * Encodes the given username and password as basic auth. The header value will be encoded with
     * {@link StandardCharsets#UTF_8 UTF_8}.
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return The encoded basic auth header value.
     */
    public static String encodeBasicAuth(final String username, final String password) {
        requireNonNull(username, "username");
        requireNonNull(password, "password");
        final String auth = username + ':' + password;
        byte[] encoded;
        try {
            encoded = Base64.getEncoder().encode(auth.getBytes(UTF_8));
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to encode basic authentication token", e);
        }
        return "Basic " + new String(encoded, UTF_8);
    }

    private AuthenticatingClientInterceptors() {}

}

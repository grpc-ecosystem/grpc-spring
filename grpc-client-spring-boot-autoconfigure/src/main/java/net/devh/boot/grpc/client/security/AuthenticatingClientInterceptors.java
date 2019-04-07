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

package net.devh.boot.grpc.client.security;

import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.common.security.SecurityConstants.AUTHORIZATION_HEADER;

import java.nio.charset.StandardCharsets;

import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.StubTransformer;

/**
 * Helper class that can be used to create {@link CallCredentials} and the necessary {@link ClientInterceptor}s.
 *
 * <p>
 * This class can be used like this:
 * </p>
 *
 * <pre>
 * <code>&#64;Bean
 * CallCredentials grpcCallCredentials() {
 *     // Note: This method uses experimental grpc-java-API features.
 *     return CallCredentialsHelper.basicAuthCallCredentials("foo", "bar");
 *     // return CallCredentialsHelper.requirePrivacy(...); // Always a good idea
 * }
 *
 * &#64;Bean
 * ClientInterceptor grpcCallCredentialsInterceptor() {
 *     return AuthenticatingClientInterceptors.callCredentialsInterceptor(grpcCallCredentials());
 * }</code>
 * </pre>
 *
 * <p>
 * <b>Note:</b> You don't need extra call credentials if you authenticate yourself via certificates.
 * </p>
 *
 * <ul>
 * <li>If you only need a single CallCredentials for all clients, then you can register it globally.
 *
 * <pre>
 * <code>&#64;Bean
 * public GlobalClientInterceptorConfigurer basicAuthInterceptorConfigurer() {
 *     return registry -&gt; registry.addClientInterceptors(grpcCallCredentialsInterceptor());
 * }</code>
 * </pre>
 *
 * </li>
 *
 * <li>If you need different authenticating client interceptors, then you can use the
 * {@link GrpcClient#interceptorNames()} field to add a bean by name.
 *
 * <pre>
 * <code>&#64;GrpcClient(value = "testChannel", interceptorNames = "grpcCallCredentialsInterceptorForTest")
 * private Channel testChannel;</code>
 * </pre>
 *
 * </li>
 * </ul>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @deprecated Use the {@link CallCredentialsHelper} or create the {@link CallCredentials} and {@link StubTransformer}s
 *             yourself.
 */
@Deprecated
public final class AuthenticatingClientInterceptors {

    /**
     * Creates a new {@link ClientInterceptor} that adds the given username and passwords as basic auth to all requests.
     * The header will be encoded with {@link StandardCharsets#UTF_8 UTF_8}.
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return The newly created basic auth interceptor.
     * @see CallCredentialsHelper#encodeBasicAuth(String, String)
     * @deprecated Use the (potentially) more secure {@link CallCredentials}.
     */
    @Deprecated
    public static ClientInterceptor basicAuthInterceptor(final String username, final String password) {
        final Metadata extraHeaders = new Metadata();
        extraHeaders.put(AUTHORIZATION_HEADER, CallCredentialsHelper.encodeBasicAuth(username, password));
        return MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
    }

    /**
     * Creates a new {@link ClientInterceptor} that will attach the given call credentials to the given call. This is an
     * alternative to manually configuring the stubs using {@link AbstractStub#withCallCredentials(CallCredentials)}.
     *
     * @param callCredentials The call credentials to attach.
     * @return The newly created client credentials interceptor.
     * @see CallCredentialsHelper#basicAuth(String, String) Basic-Auth
     * @deprecated Use {@link StubTransformer}s to set the credentials directly on {@link AbstractStub}s.
     */
    @Deprecated
    public static ClientInterceptor callCredentialsInterceptor(final CallCredentials callCredentials) {
        return new CallCredentialsAttachingClientInterceptor(callCredentials);
    }

    private static final class CallCredentialsAttachingClientInterceptor implements ClientInterceptor {

        private final CallCredentials credentials;

        CallCredentialsAttachingClientInterceptor(final CallCredentials credentials) {
            this.credentials = requireNonNull(credentials, "credentials");
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> method,
                final CallOptions callOptions, final Channel next) {
            return next.newCall(method, callOptions.withCallCredentials(this.credentials));
        }

    }

    /**
     * Creates a new {@link ClientInterceptor} that adds the given bearer token as Bearer Authentication to all
     * requests. The header will be encoded with {@link StandardCharsets#UTF_8 UTF_8}.
     *
     * @param token the bearer token
     * @return The newly created basic auth interceptor.
     * @deprecated Use {@link StubTransformer}s to set the credentials directly on {@link AbstractStub}s.
     */
    @Deprecated
    public static ClientInterceptor bearerAuth(final String token) {
        final Metadata extraHeaders = new Metadata();
        extraHeaders.put(AUTHORIZATION_HEADER, "Bearer " + token);
        return MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
    }

    private AuthenticatingClientInterceptors() {}

}

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

package net.devh.boot.grpc.client.security;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.common.security.SecurityConstants.AUTHORIZATION_HEADER;
import static net.devh.boot.grpc.common.security.SecurityConstants.BASIC_AUTH_PREFIX;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.CallCredentials2;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.SecurityLevel;
import io.grpc.Status;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import net.devh.boot.grpc.client.inject.GrpcClient;

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
 *     return AuthenticatingClientInterceptors.basicAuthCallCredentials("foo", "bar");
 *     // return AuthenticatingClientInterceptors.requirePrivacy(...); // Always a good idea
 * }
 *
 * &#64;Bean
 * ClientInterceptor grpcCallCredentialsInterceptor() {
 *     return AuthenticatingClientInterceptors.callCredentialsInterceptor(grpcCallCredentials());
 * }</code>
 * </pre>
 *
 * <p>
 * Currently supported credentials:
 * </p>
 * <ul>
 * <li>{@link #basicAuthCallCredentials(String, String) Basic-Auth}</li>
 * </ul>
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
 */
// @ExperimentalApi("https://github.com/grpc/grpc-java/issues/1914")
// @ExperimentalApi("https://github.com/grpc/grpc-java/issues/4901")
public final class AuthenticatingClientInterceptors {

    /**
     * Creates a new {@link ClientInterceptor} that adds the given username and passwords as basic auth to all requests.
     * The header will be encoded with {@link StandardCharsets#UTF_8 UTF_8}.
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return The newly created basic auth interceptor.
     * @see #encodeBasicAuth(String, String)
     * @deprecated Use the (potentially) more secure {@link CallCredentials}.
     */
    @Deprecated
    public static ClientInterceptor basicAuthInterceptor(final String username, final String password) {
        final Metadata extraHeaders = new Metadata();
        extraHeaders.put(AUTHORIZATION_HEADER, encodeBasicAuth(username, password));
        return MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
    }

    /**
     * Creates a new {@link ClientInterceptor} that will attach the given call credentials to the given call. This is an
     * alternative to manually configuring the stubs using {@link AbstractStub#withCallCredentials(CallCredentials)}.
     *
     * @param callCredentials The call credentials to attach.
     * @return The newly created client credentials interceptor.
     * @see #basicAuthCallCredentials(String, String) Basic-Auth
     */
    // TODO: Check alternatives such as using a StubTransformer for the GrpcClientBeanPostProcessor
    public static ClientInterceptor callCredentialsInterceptor(final CallCredentials callCredentials) {
        return new CallCredentialsAttachingClientInterceptor(callCredentials);
    }

    private static final class CallCredentialsAttachingClientInterceptor implements ClientInterceptor {

        private final CallCredentials credentials;

        CallCredentialsAttachingClientInterceptor(final CallCredentials credentials) {
            this.credentials = checkNotNull(credentials, "credentials");
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> method,
                final CallOptions callOptions, final Channel next) {
            return next.newCall(method, callOptions.withCallCredentials(this.credentials));
        }

    }

    /**
     * Creates a new call credential with the given username and password for basic auth.
     *
     * <p>
     * <b>Note:</b> This method uses experimental grpc-java-API features.
     * </p>
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return The newly created basic auth credentials.
     */
    public static CallCredentials2 basicAuthCallCredentials(final String username, final String password) {
        final Metadata extraHeaders = new Metadata();
        extraHeaders.put(AUTHORIZATION_HEADER, encodeBasicAuth(username, password));
        return new StaticSecurityHeaderCallCredentials(extraHeaders);
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
        return BASIC_AUTH_PREFIX + new String(encoded, UTF_8);
    }

    private static final class StaticSecurityHeaderCallCredentials extends CallCredentials2 {

        private final Metadata extraHeaders;

        StaticSecurityHeaderCallCredentials(final Metadata extraHeaders) {
            this.extraHeaders = requireNonNull(extraHeaders, "extraHeaders");
        }

        @SuppressWarnings("deprecation") // API evolution in progress
        @Override
        public void applyRequestMetadata(final RequestInfo requestInfo, final Executor appExecutor,
                final MetadataApplier applier) {
            applier.apply(this.extraHeaders);
        }

        @Override
        public void thisUsesUnstableApi() {} // API evolution in progress

    }

    /**
     * Checks whether the given security level provides privacy for all data being send on the connection.
     *
     * <p>
     * <b>Note:</b> This method uses experimental grpc-java-API features.
     * </p>
     *
     * @param securityLevel The security level to check.
     * @return True, if and only if the given security level ensures privacy. False otherwise.
     */
    public static boolean isPrivacyGuaranteed(final SecurityLevel securityLevel) {
        return SecurityLevel.PRIVACY_AND_INTEGRITY == securityLevel;
    }

    /**
     * Wraps the given call credentials in a new layer, which ensures that the credentials are only send, if the
     * connection guarantees privacy. If the connection doesn't do that, the call will be aborted before sending any
     * data.
     *
     * <p>
     * <b>Note:</b> This method uses experimental grpc-java-API features.
     * </p>
     *
     * @param callCredentials The call credentials to wrap.
     * @return The newly created call credentials.
     */
    public static CallCredentials requirePrivacy(final CallCredentials2 callCredentials) {
        return new RequirePrivacyCallCredentials(callCredentials);
    }

    private static final class RequirePrivacyCallCredentials extends CallCredentials2 {

        private static final Status STATUS_LACKING_PRIVACY = Status.UNAUTHENTICATED
                .withDescription("Connection security level does not ensure credential privacy");

        private final CallCredentials2 callCredentials;

        RequirePrivacyCallCredentials(final CallCredentials2 callCredentials) {
            this.callCredentials = callCredentials;
        }

        @SuppressWarnings("deprecation") // API evolution in progress
        @Override
        public void applyRequestMetadata(final RequestInfo requestInfo, final Executor appExecutor,
                final MetadataApplier applier) {
            if (isPrivacyGuaranteed(requestInfo.getSecurityLevel())) {
                this.callCredentials.applyRequestMetadata(requestInfo, appExecutor, applier);
            } else {
                applier.fail(STATUS_LACKING_PRIVACY);
            }
        }

        @Override
        public void thisUsesUnstableApi() {} // API evolution in progress

    }


    /**
     * Wraps the given call credentials in a new layer, that will only include the credentials if the connection
     * guarantees privacy. If the connection doesn't do that, the call will continue without the credentials.
     *
     * <p>
     * <b>Note:</b> This method uses experimental grpc-java-API features.
     * </p>
     *
     * @param callCredentials The call credentials to wrap.
     * @return The newly created call credentials.
     */
    public static CallCredentials includeWhenPrivate(final CallCredentials2 callCredentials) {
        return new IncludeWhenPrivateCallCredentials(callCredentials);
    }

    private static final class IncludeWhenPrivateCallCredentials extends CallCredentials2 {

        private final CallCredentials2 callCredentials;

        IncludeWhenPrivateCallCredentials(final CallCredentials2 callCredentials) {
            this.callCredentials = callCredentials;
        }

        @Override
        public void applyRequestMetadata(final RequestInfo requestInfo, final Executor appExecutor,
                final MetadataApplier applier) {
            if (isPrivacyGuaranteed(requestInfo.getSecurityLevel())) {
                this.callCredentials.applyRequestMetadata(requestInfo, appExecutor, applier);
            }
        }

        @Override
        public void thisUsesUnstableApi() {} // API evolution in progress

    }

    private AuthenticatingClientInterceptors() {}

}

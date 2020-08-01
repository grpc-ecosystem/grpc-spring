/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.common.security.SecurityConstants.AUTHORIZATION_HEADER;
import static net.devh.boot.grpc.common.security.SecurityConstants.BASIC_AUTH_PREFIX;
import static net.devh.boot.grpc.common.security.SecurityConstants.BEARER_AUTH_PREFIX;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.SecurityLevel;
import io.grpc.Status;
import io.grpc.stub.AbstractStub;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientSecurityAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.StubTransformer;
import net.devh.boot.grpc.common.security.SecurityConstants;

/**
 * Helper class with useful methods to create and configure some commonly used authentication schemes such as
 * {@code Basic-Auth}.
 *
 * <p>
 * <b>Note:</b> If you have exactly one {@link CallCredentials} bean in your application context then it will be used
 * for all {@link AbstractStub} that are annotation with {@link GrpcClient}. If you have none or multiple
 * {@link CallCredentials} in the application context or use {@link Channel}s, then you have to configure the
 * credentials yourself (See {@link GrpcClientSecurityAutoConfiguration}).
 * </p>
 *
 * <p>
 * Currently the following {@link CallCredentials} are supported by this class:
 * </p>
 * <ul>
 * <li>{@link #basicAuth(String, String) Basic-Auth}</li>
 * <li>{@link #requirePrivacy(CallCredentials) Require privacy for the connection} (Wrapper)</li>
 * <li>{@link #includeWhenPrivate(CallCredentials) Include credentials only if connection is private} (Wrapper)</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 *
 * <ul>
 * <li>If you need only a single CallCredentials for all services, then it suffices to declare it as bean in your
 * application context/configuration.
 *
 * <pre>
 * <code>@Bean
 * CallCredentials myCallCredentials() {
 *     return CallCredentialsHelper#basicAuth("user", "password")}
 * }</code>
 * </pre>
 *
 * </li>
 * <li>If you need multiple/different CallCredentials for the services or only need them for a subset, then you should
 * either add none of them or all of them (two ore more) to your application context to prevent the automatic credential
 * selection. You can use a {@link StubTransformer} to select a CallCredential based on the client name instead.
 *
 * <pre>
 * <code>@Bean
 * StubTransformer myCallCredentialsTransformer() {
 *     return CallCredentialsHelper#mappedCredentialsStubTransformer(Map.of(
 *         "myService1", basicAuth("user1", "password1"),
 *         "theService2", basicAuth("foo", "bar"),
 *         "publicApi", null // No credentials needed
 *     ));
 * }</code>
 * </pre>
 *
 * </li>
 * <li>If you need different CallCredentials for each call, then you have to define it in the method yourself.
 *
 * <pre>
 * <code>stub.withCallCredentials(CallCredentialsHelper#basicAuth("user", "password")).doStuff(request);</code>
 * </pre>
 *
 * </li>
 * </ul>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
// @ExperimentalApi("https://github.com/grpc/grpc-java/issues/1914")
// @ExperimentalApi("https://github.com/grpc/grpc-java/issues/4901")
public class CallCredentialsHelper {

    /**
     * Creates a new {@link StubTransformer} that will assign the given credentials to the given {@link AbstractStub}.
     *
     * @param credentials The call credentials to assign.
     * @return The transformed stub.
     * @see AbstractStub#withCallCredentials(CallCredentials)
     */
    public static StubTransformer fixedCredentialsStubTransformer(final CallCredentials credentials) {
        requireNonNull(credentials, "credentials");
        return (name, stub) -> stub.withCallCredentials(credentials);
    }

    /**
     * Creates a new {@link StubTransformer} that will assign credentials to the given {@link AbstractStub} based on the
     * name. If the given map does not contain a value for the given name, then the call credentials will be omitted.
     *
     * @param credentialsByName The map that contains the call credentials.
     * @return The transformed stub.
     * @see #mappedCredentialsStubTransformer(Map, CallCredentials)
     * @see AbstractStub#withCallCredentials(CallCredentials)
     */
    public static StubTransformer mappedCredentialsStubTransformer(
            final Map<String, CallCredentials> credentialsByName) {
        return mappedCredentialsStubTransformer(credentialsByName, null);
    }

    /**
     * Creates a new {@link StubTransformer} that will assign credentials to the given {@link AbstractStub} based on the
     * name. If the given map does not contain a value for the given name, then the optional fallback will be used
     * otherwise the call credentials will be omitted.
     *
     * @param credentialsByName The map that contains the call credentials.
     * @param fallback The optional fallback to use.
     * @return The transformed stub.
     * @see AbstractStub#withCallCredentials(CallCredentials)
     */
    public static StubTransformer mappedCredentialsStubTransformer(
            final Map<String, CallCredentials> credentialsByName,
            @Nullable final CallCredentials fallback) {
        requireNonNull(credentialsByName, "credentials");
        return (name, stub) -> {
            final CallCredentials credentials = credentialsByName.getOrDefault(name, fallback);
            if (credentials == null) {
                return stub;
            } else {
                return stub.withCallCredentials(credentials);
            }
        };
    }

    /**
     * Creates new call credentials with the given token for bearer auth.
     *
     * <p>
     * <b>Note:</b> This method uses experimental grpc-java-API features.
     * </p>
     *
     * @param token the bearer token to use
     * @return The newly created bearer auth credentials.
     * @see SecurityConstants#BEARER_AUTH_PREFIX
     * @see #authorizationHeader(String)
     */
    public static CallCredentials bearerAuth(final String token) {
        return authorizationHeader(BEARER_AUTH_PREFIX + token);
    }

    /**
     * Creates new call credentials with the given username and password for basic auth.
     *
     * <p>
     * <b>Note:</b> This method uses experimental grpc-java-API features.
     * </p>
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return The newly created basic auth credentials.
     * @see SecurityConstants#BASIC_AUTH_PREFIX
     * @see #encodeBasicAuth(String, String)
     * @see #authorizationHeader(String)
     */
    public static CallCredentials basicAuth(final String username, final String password) {
        return authorizationHeader(encodeBasicAuth(username, password));
    }

    /**
     * Encodes the given username and password as basic auth. The header value will be encoded with
     * {@link StandardCharsets#UTF_8 UTF_8}.
     *
     * @param username The username to use.
     * @param password The password to use.
     * @return The encoded basic auth header value.
     * @see SecurityConstants#BASIC_AUTH_PREFIX
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

    /**
     * Creates new call credentials with the given static authorization information.
     *
     * <p>
     * <b>Note:</b> This method uses experimental grpc-java-API features.
     * </p>
     *
     * @param authorization The authorization to use. The authorization usually starts with the scheme such as as
     *        {@code "Basic "} or {@code "Bearer "} followed by the actual authentication information.
     * @return The newly created call credentials.
     * @see SecurityConstants#AUTHORIZATION_HEADER
     * @see #authorizationHeaders(Metadata)
     */
    public static CallCredentials authorizationHeader(final String authorization) {
        requireNonNull(authorization);
        final Metadata extraHeaders = new Metadata();
        extraHeaders.put(AUTHORIZATION_HEADER, authorization);
        return authorizationHeaders(extraHeaders);
    }

    /**
     * Creates new call credentials with the given static authorization headers.
     *
     * @param authorizationHeaders The authorization headers to use.
     * @return The newly created call credentials.
     */
    public static CallCredentials authorizationHeaders(final Metadata authorizationHeaders) {
        return new StaticSecurityHeaderCallCredentials(requireNonNull(authorizationHeaders));
    }

    /**
     * The static security header {@link CallCredentials} simply add a set of predefined headers to the call. Their
     * specific meaning is server specific. This implementation can be used, for example, for BasicAuth.
     */
    private static final class StaticSecurityHeaderCallCredentials extends CallCredentials {

        private final Metadata extraHeaders;

        StaticSecurityHeaderCallCredentials(final Metadata extraHeaders) {
            this.extraHeaders = requireNonNull(extraHeaders, "extraHeaders");
        }

        @Override
        public void applyRequestMetadata(final RequestInfo requestInfo, final Executor appExecutor,
                final MetadataApplier applier) {
            applier.apply(this.extraHeaders);
        }

        @Override
        public void thisUsesUnstableApi() {} // API evolution in progress

        @Override
        public String toString() {
            return "StaticSecurityHeaderCallCredentials [extraHeaders.keys=" + this.extraHeaders.keys() + "]";
        }

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
    public static CallCredentials requirePrivacy(final CallCredentials callCredentials) {
        return new RequirePrivacyCallCredentials(callCredentials);
    }

    /**
     * A call credentials implementation with slightly increased security requirements. It ensures that the credentials
     * aren't send via an insecure connection. However, it does not prevent requests via insecure connections. This
     * wrapper does not have any other influence on the security of the underlying {@link CallCredentials}
     * implementation.
     */
    private static final class RequirePrivacyCallCredentials extends CallCredentials {

        private static final Status STATUS_LACKING_PRIVACY = Status.UNAUTHENTICATED
                .withDescription("Connection security level does not ensure credential privacy");

        private final CallCredentials callCredentials;

        RequirePrivacyCallCredentials(final CallCredentials callCredentials) {
            this.callCredentials = callCredentials;
        }

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

        @Override
        public String toString() {
            return "RequirePrivacyCallCredentials [callCredentials=" + this.callCredentials + "]";
        }

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
    public static CallCredentials includeWhenPrivate(final CallCredentials callCredentials) {
        return new IncludeWhenPrivateCallCredentials(callCredentials);
    }

    /**
     * A call credentials implementation with increased security requirements. It ensures that the credentials and
     * requests aren't send via an insecure connection. This wrapper does not have any other influence on the security
     * of the underlying {@link CallCredentials} implementation.
     */
    private static final class IncludeWhenPrivateCallCredentials extends CallCredentials {

        private final CallCredentials callCredentials;

        IncludeWhenPrivateCallCredentials(final CallCredentials callCredentials) {
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

        @Override
        public String toString() {
            return "IncludeWhenPrivateCallCredentials [callCredentials=" + this.callCredentials + "]";
        }

    }

    private CallCredentialsHelper() {}

}

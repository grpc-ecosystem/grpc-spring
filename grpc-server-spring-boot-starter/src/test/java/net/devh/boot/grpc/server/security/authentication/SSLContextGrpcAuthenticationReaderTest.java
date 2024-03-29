/*
 * Copyright (c) 2016-2024 The gRPC-Spring Authors
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.cert.X509Certificate;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.TlsChannelCredentials;
import io.grpc.TlsServerCredentials;
import io.grpc.TlsServerCredentials.ClientAuth;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import io.grpc.util.AdvancedTlsX509KeyManager;
import io.grpc.util.AdvancedTlsX509TrustManager;
import net.devh.boot.grpc.server.security.authentication.CertificateHelper.CertificateAndKeys;

class SSLContextGrpcAuthenticationReaderTest {

    final CertificateHelper certificateHelper = new CertificateHelper();
    CertificateAndKeys root;
    CertificateAndKeys intermediate;
    CertificateAndKeys server;
    CertificateAndKeys client;
    CertificateAndKeys clientWithIntermediate;

    @BeforeEach
    void setUp() throws Exception {
        this.root = certificateHelper.rootCertificate("CN=Root");
        this.intermediate = certificateHelper.intermediateCertificate("CN=Intermediate", root);
        this.server = certificateHelper.leafCertificate("CN=Server", intermediate);
        this.client = certificateHelper.leafCertificate("CN=Client", root);
        this.clientWithIntermediate = certificateHelper.leafCertificate("CN=ClientWithIntermediate", intermediate);
    }

    @Test
    void readAuthentication() throws Exception {
        var serverKeyManager = new AdvancedTlsX509KeyManager();
        serverKeyManager.updateIdentityCredentials(server.keyPair().getPrivate(),
                new X509Certificate[] {server.certificate(), intermediate.certificate()});

        var clientKeyManager = new AdvancedTlsX509KeyManager();
        clientKeyManager.updateIdentityCredentials(client.keyPair().getPrivate(),
                new X509Certificate[] {client.certificate()});

        var authentication = readAuthentication(serverKeyManager, clientKeyManager);
        assertNotNull(authentication);
        assertInstanceOf(X509Certificate.class, authentication.getCredentials());
        X509Certificate certificate = (X509Certificate) authentication.getCredentials();
        assertEquals("CN=Client", certificate.getSubjectX500Principal().toString());
    }

    @Test
    void readAuthenticationWithIntermediateCertificate() throws Exception {
        var serverKeyManager = new AdvancedTlsX509KeyManager();
        serverKeyManager.updateIdentityCredentials(server.keyPair().getPrivate(),
                new X509Certificate[] {server.certificate(), intermediate.certificate()});

        var clientKeyManager = new AdvancedTlsX509KeyManager();
        clientKeyManager.updateIdentityCredentials(clientWithIntermediate.keyPair().getPrivate(),
                new X509Certificate[] {clientWithIntermediate.certificate(), intermediate.certificate()});

        var authentication = readAuthentication(serverKeyManager, clientKeyManager);
        assertNotNull(authentication);
        assertInstanceOf(X509Certificate.class, authentication.getCredentials());
        X509Certificate certificate = (X509Certificate) authentication.getCredentials();
        assertEquals("CN=ClientWithIntermediate", certificate.getSubjectX500Principal().toString());
    }

    private Authentication readAuthentication(AdvancedTlsX509KeyManager serverKeyManager,
            AdvancedTlsX509KeyManager clientKeyManager) throws Exception {
        var trustManager = AdvancedTlsX509TrustManager.newBuilder().build();
        trustManager.updateTrustCredentials(new X509Certificate[] {root.certificate()});

        var serverCredentials = TlsServerCredentials.newBuilder()
                .trustManager(trustManager)
                .keyManager(serverKeyManager)
                .clientAuth(ClientAuth.REQUIRE)
                .build();

        var interceptor = new AuthenticationReaderServerInterceptor();
        var healthStatusManager = new HealthStatusManager();
        var server = Grpc.newServerBuilderForPort(0, serverCredentials)
                .addService(healthStatusManager.getHealthService())
                .intercept(interceptor)
                .build();
        server.start();
        ManagedChannel channel = null;
        try {
            var clientCredentials = TlsChannelCredentials.newBuilder()
                    .trustManager(trustManager)
                    .keyManager(clientKeyManager)
                    .build();

            channel = Grpc.newChannelBuilderForAddress("localhost", server.getPort(), clientCredentials).build();
            var client = HealthGrpc.newStub(channel);

            var clientCallComplete = new CompletableFuture<Void>();
            client.check(HealthCheckRequest.getDefaultInstance(), new FutureStreamObserver(clientCallComplete));
            clientCallComplete.get();

            return interceptor.authenticationFuture().get();
        } finally {
            if (channel != null) {
                channel.shutdownNow();
            }
            server.shutdownNow();
        }
    }

    record FutureStreamObserver(CompletableFuture<Void> clientCallComplete) implements StreamObserver<HealthCheckResponse> {

    @Override
    public void onNext(HealthCheckResponse healthCheckResponse) {}

    @Override
    public void onError(Throwable throwable) {
        clientCallComplete.completeExceptionally(throwable);
    }

    @Override
    public void onCompleted() {
        clientCallComplete.complete(null);
    }

    }

    record AuthenticationReaderServerInterceptor(
            SSLContextGrpcAuthenticationReader reader,
            CompletableFuture<Authentication> authenticationFuture) implements ServerInterceptor {

    public AuthenticationReaderServerInterceptor() {
                this(new SSLContextGrpcAuthenticationReader(), new CompletableFuture<>());
        }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        var authentication = reader.readAuthentication(call, headers);
        authenticationFuture.complete(authentication);
        return next.startCall(call, headers);
    }
}}

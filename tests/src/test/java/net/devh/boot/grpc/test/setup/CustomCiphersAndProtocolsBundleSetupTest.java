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

package net.devh.boot.grpc.test.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.net.ssl.SSLHandshakeException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

@SpringBootTest(properties = {
        "grpc.server.security.enabled=true",
        "grpc.server.security.bundle=server",
        "spring.ssl.bundle.pem.server.keystore.certificate=file:src/test/resources/certificates/server.crt",
        "spring.ssl.bundle.pem.server.keystore.privateKey=file:src/test/resources/certificates/server.key",
        "spring.ssl.bundle.pem.server.options.ciphers=TLS_AES_256_GCM_SHA384,ECDHE-RSA-AES256-GCM-SHA384",
        "spring.ssl.bundle.pem.server.options.enabledProtocols=TLSv1.3,TLSv1.2",

        "grpc.client.GLOBAL.address=localhost:9090",
        "grpc.client.GLOBAL.security.authorityOverride=localhost",
        "grpc.client.GLOBAL.security.trustCertCollection=file:src/test/resources/certificates/trusted-servers-collection",
        "grpc.client.GLOBAL.negotiationType=TLS",

        "spring.ssl.bundle.pem.tls11.options.enabledProtocols=TLSv1.1",
        "spring.ssl.bundle.pem.tls11.options.ciphers=ECDHE-RSA-AES256-SHA",
        "grpc.client.tls11.security.bundle=tls11",

        "spring.ssl.bundle.pem.tls12.options.enabledProtocols=TLSv1.2",
        "spring.ssl.bundle.pem.tls12.options.ciphers=ECDHE-RSA-AES256-GCM-SHA384",
        "grpc.client.tls12.security.bundle=tls12",

        "spring.ssl.bundle.pem.tls13.options.enabledProtocols=TLSv1.3",
        "spring.ssl.bundle.pem.tls13.options.ciphers=TLS_AES_256_GCM_SHA384",
        "grpc.client.tls13.security.bundle=tls13",

        "spring.ssl.bundle.pem.noSharedCiphers.options.enabledProtocols=TLSv1.2,TLSv1.1",
        "spring.ssl.bundle.pem.noSharedCiphers.options.ciphers=ECDHE-RSA-AES128-GCM-SHA256,ECDHE-RSA-AES128-SHA",
        "grpc.client.noSharedCiphers.security.bundle=noSharedCiphers",

        "spring.ssl.bundle.pem.noSharedProtocols.options.enabledProtocols=TLSv1.1",
        "spring.ssl.bundle.pem.noSharedProtocols.options.ciphers=ECDHE-RSA-AES128-SHA",
        "grpc.client.noSharedProtocols.security.bundle=noSharedProtocols",
})
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class, SslAutoConfiguration.class})
@DirtiesContext
class CustomCiphersAndProtocolsBundleSetupTest extends AbstractSimpleServerClientTest {

    @GrpcClient("test")
    private TestServiceGrpc.TestServiceBlockingStub test;
    @GrpcClient("tls11")
    private TestServiceGrpc.TestServiceBlockingStub tlsV11Stub;
    @GrpcClient("tls12")
    private TestServiceGrpc.TestServiceBlockingStub tlsV12Stub;
    @GrpcClient("tls13")
    private TestServiceGrpc.TestServiceBlockingStub tlsV13Stub;
    @GrpcClient("noSharedCiphers")
    private TestServiceGrpc.TestServiceBlockingStub tlsNoSharedCiphersStub;
    @GrpcClient("noSharedProtocols")
    private TestServiceGrpc.TestServiceBlockingStub tlsNoSharedProtocolsStub;

    /**
     * Tests behaviour with TLSv1.1 and shared protocols. Test should fail, as the server does not support TLSv1.1.
     */
    @Test
    public void testTlsV11Stub() {

        Exception exception = assertThrows(StatusRuntimeException.class, () -> {
            tlsV11Stub.normal(Empty.getDefaultInstance()).getVersion();
        });
        assertTrue(exception.getCause() instanceof SSLHandshakeException);
    }

    /**
     * Tests behaviour with TLSv1.2 and shared protocols. Test should succeed, as the server supports TLSv1.2.
     */
    @Test
    public void testTlsV12Stub() {

        assertEquals("1.2.3",
                tlsV12Stub.normal(Empty.getDefaultInstance()).getVersion());
    }

    /**
     * Tests behaviour with TLSv1.3 and shared protocols. Test should succeed, as the server supports TLSv1.3.
     */
    @Test
    public void testTlsV13Stub() {

        assertEquals("1.2.3",
                tlsV13Stub.normal(Empty.getDefaultInstance()).getVersion());
    }

    /**
     * Tests behaviour with no shared ciphers. Test should fail with a {@link SSLHandshakeException}
     */
    @Test
    public void testNoSharedCiphersClientStub() {

        Exception exception = assertThrows(StatusRuntimeException.class, () -> {
            tlsNoSharedCiphersStub.normal(Empty.getDefaultInstance()).getVersion();
        });
        assertTrue(exception.getCause() instanceof SSLHandshakeException);
    }

    /**
     * Tests behaviour with no shared protocols. Test should fail with a {@link SSLHandshakeException} as the server
     * does not support TLSv1.1.
     */
    @Test
    public void testNoSharedProtocolsStub() {

        Exception exception = assertThrows(StatusRuntimeException.class, () -> {
            tlsNoSharedProtocolsStub.normal(Empty.getDefaultInstance()).getVersion();
        });
        assertTrue(exception.getCause() instanceof SSLHandshakeException);
    }
}

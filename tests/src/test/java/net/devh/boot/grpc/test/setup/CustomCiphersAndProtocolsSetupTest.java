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

package net.devh.boot.grpc.test.setup;

import static org.junit.jupiter.api.Assertions.*;

import javax.net.ssl.SSLHandshakeException;

import org.junit.jupiter.api.Test;
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
        "grpc.server.security.certificateChainPath=src/test/resources/certificates/server.crt",
        "grpc.server.security.privateKeyPath=src/test/resources/certificates/server.key",
        "grpc.server.security.ciphers=TLS_AES_256_GCM_SHA384:ECDHE-RSA-AES256-GCM-SHA384",
        "grpc.server.security.protocols=TLSv1.3:TLSv1.2",

        "grpc.client.GLOBAL.security.authorityOverride=localhost",
        "grpc.client.GLOBAL.security.trustCertCollectionPath=src/test/resources/certificates/trusted-servers-collection",
        "grpc.client.GLOBAL.negotiationType=TLS",

        "grpc.client.tls11.security.protocols=TLSv1.1",
        "grpc.client.tls11.security.ciphers=ECDHE-RSA-AES256-SHA",

        "grpc.client.tls12.security.protocols=TLSv1.2",
        "grpc.client.tls12.security.ciphers=ECDHE-RSA-AES256-GCM-SHA384",

        "grpc.client.tls13.security.protocols=TLSv1.3",
        "grpc.client.tls13.security.ciphers=TLS_AES_256_GCM_SHA384",

        "grpc.client.noSharedCiphers.security.protocols=TLSv1.2:TLSv1.1",
        "grpc.client.noSharedCiphers.security.ciphers=ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-SHA",

        "grpc.client.noSharedProtocols.security.protocols=TLSv1.1",
        "grpc.client.noSharedProtocols.security.ciphers=ECDHE-RSA-AES128-SHA",
})
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
class CustomCiphersAndProtocolsSetupTest extends AbstractSimpleServerClientTest {

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

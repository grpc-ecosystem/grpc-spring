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

package net.devh.boot.grpc.test;

import static net.devh.boot.grpc.test.util.GrpcAssertions.assertThrowsStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.Status.Code;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.util.EnableOnIPv6;

@SpringBootTest(properties = {
        "grpc.server.address=::1",
        "grpc.client.dns.negotiationType=PLAINTEXT",
        "grpc.client.dns.address=dns:/localhost:9090/",
        "grpc.client.ipv4.negotiationType=PLAINTEXT",
        "grpc.client.ipv4.address=static://127.0.0.1:9090",
        "grpc.client.ipv6.negotiationType=PLAINTEXT",
        "grpc.client.ipv6.address=static://[::1]:9090",
})
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
@EnableOnIPv6
public class NameResolverIPv6ConnectionTest {

    private static final Empty EMPTY = Empty.getDefaultInstance();

    @GrpcClient("dns")
    private TestServiceBlockingStub dnsStub;
    @GrpcClient("ipv4")
    private TestServiceBlockingStub ipv4Stub;
    @GrpcClient("ipv6")
    private TestServiceBlockingStub ipv6Stub;

    @Test
    public void testDNSConnection() {
        assertEquals("1.2.3", this.dnsStub.normal(EMPTY).getVersion());
    }

    @Test
    public void testIpv4Connection() {
        assertThrowsStatus(Code.UNAVAILABLE, () -> this.ipv4Stub.normal(EMPTY));
    }

    @Test
    public void testIpv6Connection() {
        assertEquals("1.2.3", this.ipv6Stub.normal(EMPTY).getVersion());
    }

}

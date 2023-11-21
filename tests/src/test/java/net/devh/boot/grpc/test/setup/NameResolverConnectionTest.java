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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.util.EnableOnIPv6;

@SpringBootTest(properties = {
        "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
        "grpc.client.GLOBAL.address=localhost:9090",
        "grpc.client.dns.address=dns:///localhost:9090",
        "grpc.client.localhost.address=static://localhost:9090",
        "grpc.client.ipv4.address=static://127.0.0.1:9090",
        "grpc.client.ipv6.address=static://[::1]:9090",
})
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
public class NameResolverConnectionTest {

    private static final Empty EMPTY = Empty.getDefaultInstance();

    @GrpcClient("default")
    private TestServiceBlockingStub defaultStub;
    @GrpcClient("dns")
    private TestServiceBlockingStub dnsStub;
    @GrpcClient("localhost")
    private TestServiceBlockingStub localhostStub;
    @GrpcClient("ipv4")
    private TestServiceBlockingStub ipv4Stub;
    @GrpcClient("ipv6")
    private TestServiceBlockingStub ipv6Stub;

    @Test
    public void testDefaultConnection() {
        assertEquals("1.2.3", this.defaultStub.normal(EMPTY).getVersion());
    }

    @Test
    public void testDNSConnection() {
        assertEquals("1.2.3", this.dnsStub.normal(EMPTY).getVersion());
    }

    @Test
    public void testLocalhostConnection() {
        assertEquals("1.2.3", this.localhostStub.normal(EMPTY).getVersion());
    }

    @Test
    public void testIpv4Connection() {
        assertEquals("1.2.3", this.ipv4Stub.normal(EMPTY).getVersion());
    }

    @Test
    @EnableOnIPv6
    public void testIpv6Connection() {
        assertEquals("1.2.3", this.ipv6Stub.normal(EMPTY).getVersion());
    }

}

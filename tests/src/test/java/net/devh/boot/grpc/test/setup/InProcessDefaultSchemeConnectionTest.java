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

/**
 * Tests that the default-scheme works with the in-process scheme
 *
 */
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=test",
        "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
        "grpc.default-scheme=in-process:",
})
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
public class InProcessDefaultSchemeConnectionTest {

    private static final Empty EMPTY = Empty.getDefaultInstance();

    @GrpcClient("test")
    private TestServiceBlockingStub inProcessStub;

    /**
     * Tests the connection via the implicit client address.
     */
    @Test
    public void testSelfConnection() {
        assertEquals("1.2.3", this.inProcessStub.normal(EMPTY).getVersion());
    }

}

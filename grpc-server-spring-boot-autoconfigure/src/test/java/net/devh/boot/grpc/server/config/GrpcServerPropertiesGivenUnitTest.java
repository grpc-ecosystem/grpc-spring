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

package net.devh.boot.grpc.server.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.unit.DataSize;

/**
 * Tests whether the property resolution works when using suffixes.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "grpc.server.keepAliveTime=42m",
        "grpc.server.maxInboundMessageSize=5MB",
        "grpc.server.maxInboundMetadataSize=10KB"
})
class GrpcServerPropertiesGivenUnitTest {

    @Autowired
    private GrpcServerProperties grpcServerProperties;

    @Test
    void test() {
        assertEquals(Duration.ofMinutes(42), this.grpcServerProperties.getKeepAliveTime());
        assertEquals(DataSize.ofMegabytes(5), this.grpcServerProperties.getMaxInboundMessageSize());
        assertEquals(DataSize.ofKilobytes(10), this.grpcServerProperties.getMaxInboundMetadataSize());
    }

}

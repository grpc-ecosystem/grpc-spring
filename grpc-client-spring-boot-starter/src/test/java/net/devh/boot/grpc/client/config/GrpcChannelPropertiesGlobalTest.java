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

package net.devh.boot.grpc.client.config;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests whether the global property fallback works.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "grpc.client.GLOBAL.keepAliveTime=23m",
        "grpc.client.GLOBAL.keepAliveTimeout=31s",
        "grpc.client.test.keepAliveTime=42m"})
class GrpcChannelPropertiesGlobalTest {

    @Autowired
    private GrpcChannelsProperties grpcChannelsProperties;

    @Test
    void test() {
        assertSame(this.grpcChannelsProperties.getGlobalChannel(),
                this.grpcChannelsProperties.getChannel(GrpcChannelsProperties.GLOBAL_PROPERTIES_KEY));

        assertEquals(Duration.ofMinutes(23), this.grpcChannelsProperties.getGlobalChannel().getKeepAliveTime());
        assertEquals(Duration.ofSeconds(31), this.grpcChannelsProperties.getGlobalChannel().getKeepAliveTimeout());

        assertEquals(Duration.ofMinutes(42), this.grpcChannelsProperties.getChannel("test").getKeepAliveTime());
        assertEquals(Duration.ofSeconds(31), this.grpcChannelsProperties.getChannel("test").getKeepAliveTimeout());
    }

}

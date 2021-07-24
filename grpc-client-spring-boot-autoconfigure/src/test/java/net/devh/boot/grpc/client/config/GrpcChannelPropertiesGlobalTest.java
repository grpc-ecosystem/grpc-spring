/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

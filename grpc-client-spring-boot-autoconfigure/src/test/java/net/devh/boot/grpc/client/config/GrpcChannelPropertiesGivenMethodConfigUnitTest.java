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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @Title Test
 * @Description GrpcChannelPropertiesGivenMethodConfigUnitTest
 * @Program grpc-spring-boot-starter
 * @Author wushengju
 * @Version 1.0
 * @Date 2021-08-12 16:46
 * @Copyright Copyright (c) 2021 TCL Inc. All rights reserved
 **/
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "grpc.client.test.retry-enabled=true",
        "grpc.client.test.method-config[0].name[0].service=helloworld.Greeter",
        "grpc.client.test.method-config[0].name[0].method=SayHello",
        "grpc.client.test.method-config[0].retry-policy.max-attempts=2",
        "grpc.client.test.method-config[0].retry-policy.initial-backoff=1",
        "grpc.client.test.method-config[0].retry-policy.max-backoff=1",
        "grpc.client.test.method-config[0].retry-policy.backoff-multiplier=2",
        "grpc.client.test.method-config[0].retry-policy.retryable-status-codes=UNKNOWN,UNAVAILABLE"
})
public class GrpcChannelPropertiesGivenMethodConfigUnitTest {

    @Autowired
    private GrpcChannelsProperties grpcChannelsProperties;

    @Test
    void test() {
        final GrpcChannelProperties properties = this.grpcChannelsProperties.getChannel("test");
        assertEquals(true, properties.getRetryEnabled());
        assertEquals("helloworld.Greeter", properties.getMethodConfig().get(0).getName().get(0).getService());
        assertEquals("SayHello", properties.getMethodConfig().get(0).getName().get(0).getMethod());
        assertEquals(2, properties.getMethodConfig().get(0).getRetryPolicy().getMaxAttempts());
        assertEquals(1, properties.getMethodConfig().get(0).getRetryPolicy().getInitialBackoff().getSeconds());
        assertEquals(1, properties.getMethodConfig().get(0).getRetryPolicy().getMaxBackoff().getSeconds());
        assertEquals(2, properties.getMethodConfig().get(0).getRetryPolicy().getBackoffMultiplier());

        assertEquals(2, properties.getMethodConfig().get(0).getRetryPolicy().getRetryableStatusCodes().size());
    }
}

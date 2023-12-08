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
 * Test retry configuration using properties and inheritance.
 **/
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "grpc.client.GLOBAL.retry-enabled=true",
        "grpc.client.GLOBAL.method-config[0].name[0].service=helloworld.Greeter",
        "grpc.client.GLOBAL.method-config[0].name[0].method=SayHello",
        "grpc.client.GLOBAL.method-config[0].retry-policy.max-attempts=2",
        "grpc.client.GLOBAL.method-config[0].retry-policy.initial-backoff=1",
        "grpc.client.GLOBAL.method-config[0].retry-policy.max-backoff=1",
        "grpc.client.GLOBAL.method-config[0].retry-policy.backoff-multiplier=2",
        "grpc.client.GLOBAL.method-config[0].retry-policy.retryable-status-codes=UNKNOWN,UNAVAILABLE",
})
class GrpcChannelPropertiesMethodConfigTest {

    @Autowired
    private GrpcChannelsProperties grpcChannelsProperties;

    @Test
    void test() {
        final GrpcChannelProperties properties = this.grpcChannelsProperties.getChannel("test");
        assertEquals(true, properties.isRetryEnabled());
        final MethodConfig methodConfig = properties.getMethodConfig().get(0);
        final NameConfig nameConfig = methodConfig.getName().get(0);
        assertEquals("helloworld.Greeter", nameConfig.getService());
        assertEquals("SayHello", nameConfig.getMethod());
        final RetryPolicyConfig retryPolicy = methodConfig.getRetryPolicy();
        assertEquals(2, retryPolicy.getMaxAttempts());
        assertEquals(1, retryPolicy.getInitialBackoff().getSeconds());
        assertEquals(1, retryPolicy.getMaxBackoff().getSeconds());
        assertEquals(2, retryPolicy.getBackoffMultiplier());
        assertEquals(2, retryPolicy.getRetryableStatusCodes().size());
    }

}

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
        "grpc.server.port=0",
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

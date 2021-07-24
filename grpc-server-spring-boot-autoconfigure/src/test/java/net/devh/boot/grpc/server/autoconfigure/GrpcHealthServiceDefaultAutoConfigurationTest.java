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

package net.devh.boot.grpc.server.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthStub;

@SpringBootTest(classes = GrpcHealthServiceDefaultAutoConfigurationTest.TestConfig.class)
@ImportAutoConfiguration({
        GrpcServerAutoConfiguration.class,
        GrpcServerFactoryAutoConfiguration.class,
        GrpcHealthServiceAutoConfiguration.class})
@DirtiesContext
class GrpcHealthServiceDefaultAutoConfigurationTest {

    private static final HealthCheckRequest HEALTH_CHECK_REQUEST = HealthCheckRequest.getDefaultInstance();

    @Test
    void testHealthService() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090").usePlaintext().build();
        try {
            final HealthStub stub = HealthGrpc.newStub(channel);

            final AwaitableStreamObserver<HealthCheckResponse> resultObserver = new AwaitableStreamObserver<>();
            stub.check(HEALTH_CHECK_REQUEST, resultObserver);
            checkResult(resultObserver);
        } finally {
            channel.shutdown();
        }
    }

    void checkResult(final AwaitableStreamObserver<HealthCheckResponse> resultObserver) {
        final HealthCheckResponse response = assertDoesNotThrow(resultObserver::getSingle);
        assertEquals(ServingStatus.SERVING, response.getStatus());
    }

    static class TestConfig {
    }

}

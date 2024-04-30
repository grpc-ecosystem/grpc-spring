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
        GrpcHealthServiceAutoConfiguration.class
})
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

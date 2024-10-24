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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthStub;

@SpringBootTest(classes = {
        GrpcHealthServiceDefaultAutoConfigurationTest.TestConfig.class,
        GrpcHealthServiceTrueActuatorConfigurationTest.TestConfig.class},
        properties = {
                "grpc.server.health-service.type=ACTUATOR"})
@ImportAutoConfiguration({
        GrpcServerAutoConfiguration.class,
        GrpcServerFactoryAutoConfiguration.class,
        GrpcHealthServiceAutoConfiguration.class,
        HealthEndpointAutoConfiguration.class})
@DirtiesContext
class GrpcHealthServiceTrueActuatorConfigurationTest extends GrpcHealthServiceDefaultAutoConfigurationTest {

    @Configuration
    static class TestConfig {
        @Bean
        TestIndicator customIndicator() {
            return new TestIndicator();
        }
    }

    static class TestIndicator implements HealthIndicator {
        Health health = Health.up().build();

        @Override
        public Health health() {
            return health;
        }
    }

    @Autowired
    TestIndicator customIndicator;

    @Test
    void testUnhealthyService() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090").usePlaintext().build();
        try {
            final HealthStub stub = HealthGrpc.newStub(channel);

            customIndicator.health = Health.down().build();
            final AwaitableStreamObserver<HealthCheckResponse> resultObserver = new AwaitableStreamObserver<>();
            stub.check(HealthCheckRequest.getDefaultInstance(), resultObserver);

            final HealthCheckResponse response = assertDoesNotThrow(resultObserver::getSingle);
            assertEquals(HealthCheckResponse.ServingStatus.NOT_SERVING, response.getStatus());
        } finally {
            channel.shutdown();
        }
    }

    @Test
    void testSpecificUnhealthyService() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090").usePlaintext().build();
        try {
            final HealthStub stub = HealthGrpc.newStub(channel);

            customIndicator.health = Health.down().build();
            final AwaitableStreamObserver<HealthCheckResponse> resultObserver = new AwaitableStreamObserver<>();
            stub.check(HealthCheckRequest.newBuilder()
                    .setService("customIndicator")
                    .build(), resultObserver);

            final HealthCheckResponse response = assertDoesNotThrow(resultObserver::getSingle);
            assertEquals(HealthCheckResponse.ServingStatus.NOT_SERVING, response.getStatus());
        } finally {
            channel.shutdown();
        }
    }

    @Test
    void testNotFoundService() throws InterruptedException {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090").usePlaintext().build();
        try {
            final HealthStub stub = HealthGrpc.newStub(channel);

            customIndicator.health = Health.down().build();
            final AwaitableStreamObserver<HealthCheckResponse> resultObserver = new AwaitableStreamObserver<>();
            stub.check(HealthCheckRequest.newBuilder()
                    .setService("someservice")
                    .build(), resultObserver);

            Throwable error = resultObserver.getError();
            assertInstanceOf(StatusRuntimeException.class, error);
            assertEquals(Status.NOT_FOUND.getCode(), ((StatusRuntimeException) error).getStatus().getCode());
        } finally {
            channel.shutdown();
        }
    }

}

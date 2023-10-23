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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.Channel;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;

/**
 * These tests check the property {@link GrpcChannelProperties#getImmediateConnectTimeout()}. They check for
 * backwards-compatibility when this property didn't existed and various cases when it's enabled (for successful
 * connection and failed one).
 */
public class ImmediateConnectTests {

    @Slf4j
    @SpringBootTest(properties = {
            "grpc.client.GLOBAL.address=localhost:9090",
            "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
            "grpc.client.GLOBAL.immediateConnectTimeout=10s",
    })
    @SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
    @DirtiesContext
    static class ImmediateConnectEnabledAndSuccessfulTest extends AbstractSimpleServerClientTest {

        ImmediateConnectEnabledAndSuccessfulTest() {
            log.info("--- ImmediateConnectEnabledAnsSuccessfulTest ---");
        }

        @Test
        @DirtiesContext
        void immediateConnectEnabledAndSuccessful() {
            assumeTrue(channel instanceof ManagedChannel,
                    "To run this test channel must be ManagedChannel");
            ManagedChannel managedChannel = (ManagedChannel) channel;

            ConnectivityState state = managedChannel.getState(false);
            assertEquals(
                    "When immediateConnectTimeout property is set to positive duration channel must be in READY state if connection was successful",
                    ConnectivityState.READY, state);
        }
    }

    @Slf4j
    @SpringBootTest(properties = {
            "grpc.client.GLOBAL.address=localhost:9090",
            "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
    })
    @SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
    @DirtiesContext
    static class ImmediateConnectDisabledTest extends AbstractSimpleServerClientTest {

        ImmediateConnectDisabledTest() {
            log.info("--- ImmediateConnectDisabledTest ---");
        }

        @Test
        @DirtiesContext
        void immediateConnectDisabled() {
            assumeTrue(channel instanceof ManagedChannel,
                    "To run this test channel must be ManagedChannel");
            ManagedChannel managedChannel = (ManagedChannel) channel;

            ConnectivityState state = managedChannel.getState(false);
            assertEquals(
                    "When immediateConnectTimeout property is set to zero or unset grpc must not attempt to connect until first request",
                    ConnectivityState.IDLE, state);
        }
    }

    @Slf4j
    static class ImmediateConnectEnabledAndFailedToConnectTest {

        private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withPropertyValues(
                        "grpc.client.GLOBAL.address=localhost:9999",
                        "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
                        "grpc.client.GLOBAL.immediateConnectTimeout=1s")
                .withUserConfiguration(ServiceConfiguration.class, BaseAutoConfiguration.class)
                .withBean(FailedChannelHolder.class);

        public ImmediateConnectEnabledAndFailedToConnectTest() {
            log.info("--- ImmediateConnectEnabledAndFailedToConnectTest ---");
        }

        @Test
        void immediateConnectEnabledAndFailedToConnect() {
            contextRunner.run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                        .getCause()
                        .isOfAnyClassIn(IllegalStateException.class);
            });
        }

        private static class FailedChannelHolder {
            @GrpcClient("test")
            private Channel channel;
        }
    }
}

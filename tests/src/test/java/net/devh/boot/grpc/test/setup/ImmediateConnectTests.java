/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

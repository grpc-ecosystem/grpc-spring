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
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;

public class ImmediateConnectSuccessTest {

    @Slf4j
    @SpringBootTest(properties = {
            "grpc.client.GLOBAL.address=localhost:9090",
            "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
            "grpc.client.GLOBAL.immediateConnectTimeout=1s",
    })
    @SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
    @DirtiesContext
    static class ImmediateConnectEnabledAnsSuccessfulTest extends AbstractSimpleServerClientTest {

        ImmediateConnectEnabledAnsSuccessfulTest() {
            log.info("--- ImmediateConnectEnabledAnsSuccessfulTest ---");
        }

        @Test
        void immediateConnectEnabledAnsSuccessful() {
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

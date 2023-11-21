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

package net.devh.boot.grpc.client.channelfactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;

@ExtendWith(MockitoExtension.class)
class InProcessChannelFactoryTest {

    private static final String CHANNEL_NAME = "test";

    @Mock
    GrpcChannelsProperties channelsProperties;
    @Mock
    GrpcChannelProperties channelProperties;
    @Mock
    GlobalClientInterceptorRegistry registry;

    Server server;

    @BeforeEach
    void setupServer() throws IOException {
        server = InProcessServerBuilder.forName(CHANNEL_NAME)
                .directExecutor()
                .build();
        server.start();
    }

    @AfterEach
    void tearDownServer() throws InterruptedException {
        server.shutdown();
        server.awaitTermination();
    }

    @BeforeEach
    void setupMocks() {
        Mockito.doReturn(channelProperties)
                .when(channelsProperties)
                .getChannel(CHANNEL_NAME);
    }

    @Test
    void checkIdleStateWithoutTimeout() {
        InProcessChannelFactory factory = createFactory();
        Mockito.doReturn(ImmutableList.of())
                .when(registry)
                .getClientInterceptors();
        Mockito.doReturn(Duration.ZERO)
                .when(channelProperties)
                .getImmediateConnectTimeout();

        ManagedChannel channel = (ManagedChannel) factory.createChannel(CHANNEL_NAME);

        ConnectivityState state = channel.getState(false);
        assertEquals(ConnectivityState.IDLE, state);
    }

    @Test
    void checkThrowsIllegalStateOnInterrupt() {
        InProcessChannelFactory factory = createFactory();
        Mockito.doReturn(Duration.ofMillis(100))
                .when(channelProperties)
                .getImmediateConnectTimeout();

        Thread.currentThread().interrupt();
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> factory.createChannel(CHANNEL_NAME));
        assertEquals("Can't connect to channel " + CHANNEL_NAME, exception.getMessage());
    }

    private InProcessChannelFactory createFactory() {
        return new InProcessChannelFactory(channelsProperties, registry);
    }

}

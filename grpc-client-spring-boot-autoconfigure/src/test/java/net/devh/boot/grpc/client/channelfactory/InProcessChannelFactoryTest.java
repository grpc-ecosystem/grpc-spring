/*
 * Copyright (c) 2016-2022 Michael Zhang <yidongnan@gmail.com>
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

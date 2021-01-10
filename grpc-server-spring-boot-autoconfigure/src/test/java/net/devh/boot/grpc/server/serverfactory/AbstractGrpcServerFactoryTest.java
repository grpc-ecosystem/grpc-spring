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

package net.devh.boot.grpc.server.serverfactory;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.services.HealthStatusManager;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;

/**
 * Tests for {@link AbstractGrpcServerFactory}.
 */
class AbstractGrpcServerFactoryTest {

    /**
     * Tests {@link AbstractGrpcServerFactory#configureServices(ServerBuilder)}.
     */
    @Test
    void testConfigureServices() {
        final GrpcServerProperties properties = new GrpcServerProperties();
        properties.setReflectionServiceEnabled(false);

        final NettyGrpcServerFactory serverFactory = new NettyGrpcServerFactory(properties, emptyList());
        serverFactory.healthStatusManager = new HealthStatusManager();

        serverFactory.addService(new GrpcServiceDefinition("test1", ProtoReflectionService.class,
                ProtoReflectionService.newInstance().bindService()));
        serverFactory.addService(new GrpcServiceDefinition("test2", ProtoReflectionService.class,
                ProtoReflectionService.newInstance().bindService()));

        final NettyServerBuilder serverBuilder = serverFactory.newServerBuilder();

        assertEquals("Found duplicate service implementation: " + ServerReflectionGrpc.SERVICE_NAME,
                assertThrows(IllegalStateException.class, () -> serverFactory.configureServices(serverBuilder))
                        .getMessage());
    }

}

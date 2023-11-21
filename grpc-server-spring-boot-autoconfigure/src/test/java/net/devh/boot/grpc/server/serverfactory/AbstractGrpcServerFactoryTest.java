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

package net.devh.boot.grpc.server.serverfactory;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
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

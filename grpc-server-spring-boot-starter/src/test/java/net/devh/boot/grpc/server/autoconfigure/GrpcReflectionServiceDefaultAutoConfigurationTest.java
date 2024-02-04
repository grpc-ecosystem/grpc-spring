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
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc.ServerReflectionStub;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;

@SpringBootTest(classes = GrpcReflectionServiceDefaultAutoConfigurationTest.TestConfig.class)
@ImportAutoConfiguration({
        GrpcServerAutoConfiguration.class,
        GrpcServerFactoryAutoConfiguration.class,
        GrpcReflectionServiceAutoConfiguration.class})
@DirtiesContext
class GrpcReflectionServiceDefaultAutoConfigurationTest {

    @Test
    void testReflectionService() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090").usePlaintext().build();
        try {
            final ServerReflectionStub stub = ServerReflectionGrpc.newStub(channel);

            final AwaitableStreamObserver<ServerReflectionResponse> resultObserver = new AwaitableStreamObserver<>();
            final StreamObserver<ServerReflectionRequest> requestObserver = stub.serverReflectionInfo(resultObserver);
            requestObserver.onNext(ServerReflectionRequest.newBuilder()
                    .setListServices("")
                    .build());
            requestObserver.onCompleted();
            checkResult(resultObserver);
        } finally {
            channel.shutdown();
        }
    }

    void checkResult(final AwaitableStreamObserver<ServerReflectionResponse> resultObserver) {
        final ServerReflectionResponse response = assertDoesNotThrow(resultObserver::getSingle);
        assertEquals("grpc.reflection.v1alpha.ServerReflection",
                response.getListServicesResponse().getServiceList().get(0).getName());
    }

    static class TestConfig {
    }

}

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
import net.devh.boot.grpc.server.autoconfigure.GrpcReflectionServiceDefaultAutoConfigurationTest.TestConfig;

@SpringBootTest(classes = TestConfig.class)
@ImportAutoConfiguration({
        GrpcServerAutoConfiguration.class,
        GrpcServerFactoryAutoConfiguration.class,
        GrpcReflectionServiceAutoConfiguration.class})
@DirtiesContext
class GrpcReflectionServiceDefaultAutoConfigurationTest {

    @Test
    void testReflectionService() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090").usePlaintext().build();
        final ServerReflectionStub stub = ServerReflectionGrpc.newStub(channel);

        final AwaitableStreamObserver<ServerReflectionResponse> resultObserver = new AwaitableStreamObserver<>();
        final StreamObserver<ServerReflectionRequest> requestObserver = stub.serverReflectionInfo(resultObserver);
        requestObserver.onNext(ServerReflectionRequest.newBuilder()
                .setListServices("")
                .build());
        requestObserver.onCompleted();
        checkResult(resultObserver);
    }

    void checkResult(final AwaitableStreamObserver<ServerReflectionResponse> resultObserver) {
        final ServerReflectionResponse response = assertDoesNotThrow(resultObserver::getSingle);
        assertEquals("grpc.reflection.v1alpha.ServerReflection",
                response.getListServicesResponse().getServiceList().get(0).getName());
    }

    static class TestConfig {
    }

}

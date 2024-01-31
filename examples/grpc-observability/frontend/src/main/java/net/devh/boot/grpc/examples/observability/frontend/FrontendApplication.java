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

package net.devh.boot.grpc.examples.observability.frontend;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.examples.observability.proto.BidiStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.BidiStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.ClientStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.ClientStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.ExampleServiceGrpc.ExampleServiceStub;
import net.devh.boot.grpc.examples.observability.proto.ServerStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.ServerStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.UnaryRequest;
import net.devh.boot.grpc.examples.observability.proto.UnaryResponse;

@SpringBootApplication
public class FrontendApplication implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(FrontendApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }

    @GrpcClient("backend")
    private ExampleServiceStub stub;

    private void CallUnaryRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(10240, 20480)];
        ThreadLocalRandom.current().nextBytes(bytes);
        UnaryRequest request = UnaryRequest.newBuilder().setMessage(new String(bytes)).build();
        stub.unaryRpc(request, new StreamObserver<>() {
            @Override
            public void onNext(UnaryResponse value) {}

            @Override
            public void onError(Throwable t) {
                LOGGER.severe(Status.fromThrowable(t).toString());
                CallUnaryRpc();
            }

            @Override
            public void onCompleted() {
                CallUnaryRpc();
            }
        });
    }

    private void CallClientStreamingRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(10240, 20480)];
        ThreadLocalRandom.current().nextBytes(bytes);
        ClientStreamingRequest request = ClientStreamingRequest.newBuilder()
                .setMessage(new String(bytes)).build();
        StreamObserver<ClientStreamingRequest> requestStreamObserver = stub.clientStreamingRpc(
                new StreamObserver<>() {
                    @Override
                    public void onNext(ClientStreamingResponse value) {}

                    @Override
                    public void onError(Throwable t) {
                        CallClientStreamingRpc();
                    }

                    @Override
                    public void onCompleted() {
                        CallClientStreamingRpc();
                    }
                });
        requestStreamObserver.onNext(request);
        requestStreamObserver.onCompleted();
    }

    private void CallServerStreamingRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(10240, 20480)];
        ThreadLocalRandom.current().nextBytes(bytes);
        ServerStreamingRequest request = ServerStreamingRequest.newBuilder()
                .setMessage(new String(bytes)).build();
        stub.serverStreamingRpc(request, new StreamObserver<>() {
            @Override
            public void onNext(ServerStreamingResponse value) {}

            @Override
            public void onError(Throwable t) {
                CallServerStreamingRpc();
            }

            @Override
            public void onCompleted() {
                CallServerStreamingRpc();
            }
        });
    }

    private void CallBidStreamingRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(10240, 20480)];
        ThreadLocalRandom.current().nextBytes(bytes);
        BidiStreamingRequest request = BidiStreamingRequest.newBuilder()
                .setMessage(new String(bytes)).build();
        StreamObserver<BidiStreamingRequest> requestStreamObserver = stub.bidiStreamingRpc(
                new StreamObserver<>() {
                    @Override
                    public void onNext(BidiStreamingResponse value) {}

                    @Override
                    public void onError(Throwable t) {
                        CallBidStreamingRpc();
                    }

                    @Override
                    public void onCompleted() {
                        CallBidStreamingRpc();
                    }
                });
        requestStreamObserver.onNext(request);
        requestStreamObserver.onCompleted();
    }

    @Override
    public void run(String... args) {
        CallUnaryRpc();
        CallServerStreamingRpc();
        CallClientStreamingRpc();
        CallBidStreamingRpc();
    }
}

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

package net.devh.boot.grpc.examples.observability.backend;

import java.util.concurrent.ThreadLocalRandom;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.examples.observability.proto.BidiStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.BidiStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.ClientStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.ClientStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.ExampleServiceGrpc.ExampleServiceImplBase;
import net.devh.boot.grpc.examples.observability.proto.ServerStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.ServerStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.UnaryRequest;
import net.devh.boot.grpc.examples.observability.proto.UnaryResponse;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ExampleServiceImpl extends ExampleServiceImplBase {

    private boolean InjectError() {
        // We create ~5% error.
        return ThreadLocalRandom.current().nextInt(0, 99) >= 95;
    }

    @Override
    public void unaryRpc(UnaryRequest request,
            StreamObserver<UnaryResponse> responseObserver) {
        if (InjectError()) {
            responseObserver.onError(Status.INTERNAL.asException());
        } else {
            responseObserver.onNext(UnaryResponse.newBuilder().setMessage(request.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<ClientStreamingRequest> clientStreamingRpc(
            StreamObserver<ClientStreamingResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ClientStreamingRequest value) {
                responseObserver.onNext(
                        ClientStreamingResponse.newBuilder().setMessage(value.getMessage()).build());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                if (InjectError()) {
                    responseObserver.onError(Status.INTERNAL.asException());
                } else {
                    responseObserver.onCompleted();
                }
            }
        };
    }

    @Override
    public void serverStreamingRpc(ServerStreamingRequest request,
            StreamObserver<ServerStreamingResponse> responseObserver) {
        if (InjectError()) {
            responseObserver.onError(Status.INTERNAL.asException());
        } else {
            responseObserver.onNext(
                    ServerStreamingResponse.newBuilder().setMessage(request.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<BidiStreamingRequest> bidiStreamingRpc(
            StreamObserver<BidiStreamingResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(BidiStreamingRequest value) {
                responseObserver.onNext(
                        BidiStreamingResponse.newBuilder().setMessage(value.getMessage()).build());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                if (InjectError()) {
                    responseObserver.onError(Status.INTERNAL.asException());
                } else {
                    responseObserver.onCompleted();
                }
            }
        };
    }
}

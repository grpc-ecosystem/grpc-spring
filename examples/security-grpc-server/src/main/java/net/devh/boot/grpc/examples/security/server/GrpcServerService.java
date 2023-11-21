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

package net.devh.boot.grpc.examples.security.server;

import org.springframework.security.access.annotation.Secured;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.examples.lib.HelloReply;
import net.devh.boot.grpc.examples.lib.HelloRequest;
import net.devh.boot.grpc.examples.lib.SimpleGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * An example service that checks the user's authentication.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@GrpcService
public class GrpcServerService extends SimpleGrpc.SimpleImplBase {

    // A grpc method that requests the user to be authenticated and have the role "ROLE_GREET".
    @Override
    @Secured("ROLE_GREET")
    public void sayHello(final HelloRequest req, final StreamObserver<HelloReply> responseObserver) {
        final HelloReply reply = HelloReply.newBuilder().setMessage("Hello ==> " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}

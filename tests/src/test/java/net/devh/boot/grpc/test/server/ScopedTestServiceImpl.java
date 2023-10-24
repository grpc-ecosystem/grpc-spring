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

package net.devh.boot.grpc.test.server;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.test.proto.SomeType;

@GrpcService
public class ScopedTestServiceImpl extends TestServiceImpl {

    @Autowired
    private RequestId requestId;

    @Override
    public StreamObserver<SomeType> secureBidi(StreamObserver<SomeType> responseObserver) {
        return new StreamObserver<SomeType>() {

            @Override
            public void onNext(final SomeType input) {
                final SomeType version =
                        input.toBuilder().setVersion(ScopedTestServiceImpl.this.requestId.getId()).build();
                responseObserver.onNext(version);
            }

            @Override
            public void onError(final Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }

        };
    }

    /**
     * Fake scoped bean used to simulate variable contents. May not be a final class.
     */
    public static class RequestId {

        private final String id = UUID.randomUUID().toString();

        public String getId() {
            return this.id;
        }

        @Override
        public String toString() {
            return getId();
        }

    }

}

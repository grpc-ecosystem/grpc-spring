/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

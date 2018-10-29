/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.test.grpc.server;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;
import net.devh.test.grpc.proto.Counter;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceImplBase;
import net.devh.test.grpc.proto.Version;

@GrpcService
public class TestServiceImpl extends TestServiceImplBase {

    @Override
    public void getVersion(final Empty request, final StreamObserver<Version> responseObserver) {
        final Version version = Version.newBuilder().setVersion("1.2.3").build();
        responseObserver.onNext(version);
        responseObserver.onCompleted();
    }

    @Override
    public void increment(final Empty request, final StreamObserver<Counter> responseObserver) {
        // Not implemented (on purpose)
        super.increment(request, responseObserver);
    }

}

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

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;
import net.devh.test.grpc.proto.SomeType;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceImplBase;

@GrpcService
@Slf4j
public class TestServiceImpl extends TestServiceImplBase {

    @Override
    public void normal(final Empty request, final StreamObserver<SomeType> responseObserver) {
        log.debug("normal");
        final SomeType version = SomeType.newBuilder().setVersion("1.2.3").build();
        responseObserver.onNext(version);
        responseObserver.onCompleted();
    }

    @Override
    public void unimplemented(final Empty request, final StreamObserver<SomeType> responseObserver) {
        log.debug("unimplemented");
        // Not implemented (on purpose)
        super.unimplemented(request, responseObserver);
    }

    @Override
    @Secured("ROLE_CLIENT1")
    public void secure(final Empty request, final StreamObserver<SomeType> responseObserver) {
        log.debug("secure: {}", SecurityContextHolder.getContext().getAuthentication());
        final SomeType version = SomeType.newBuilder().setVersion("1.2.3").build();
        responseObserver.onNext(version);
        responseObserver.onCompleted();
    }

}

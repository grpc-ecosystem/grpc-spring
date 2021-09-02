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

package net.devh.boot.grpc.test.config;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceImplBase;

@Configuration
public class DynamicTestServiceConfig {

    public static BiConsumer<Empty, StreamObserver<SomeType>> UNIMPLEMENTED =
            errorWith(Status.UNIMPLEMENTED.withDescription("responseFunction not configured"));

    public static BiConsumer<Empty, StreamObserver<SomeType>> respondWith(final String data) {
        return respondWith(SomeType.newBuilder()
                .setVersion(data)
                .build());
    }

    public static BiConsumer<Empty, StreamObserver<SomeType>> respondWith(final SomeType data) {
        return (request, responseObserver) -> {
            responseObserver.onNext(data);
            responseObserver.onCompleted();
        };
    }

    public static BiConsumer<Empty, StreamObserver<SomeType>> errorWith(final Status status) {
        return (request, responseObserver) -> {
            responseObserver.onError(status.asException());
        };
    }

    public static BiConsumer<Empty, StreamObserver<SomeType>> increment(final AtomicInteger integer) {
        return (request, responseObserver) -> {
            integer.incrementAndGet();
        };
    }

    @Bean
    AtomicReference<BiConsumer<Empty, StreamObserver<SomeType>>> responseFunction() {
        return new AtomicReference<>(UNIMPLEMENTED);
    }

    @GrpcService
    TestServiceImplBase testServiceImplBase(
            final AtomicReference<BiConsumer<Empty, StreamObserver<SomeType>>> responseFunction) {

        return new TestServiceImplBase() {

            @Override
            public void normal(final Empty request, final StreamObserver<SomeType> responseObserver) {
                responseFunction.get().accept(request, responseObserver);
            }

        };
    }

}

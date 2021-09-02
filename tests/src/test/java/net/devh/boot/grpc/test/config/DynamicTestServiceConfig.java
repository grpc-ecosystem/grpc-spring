
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

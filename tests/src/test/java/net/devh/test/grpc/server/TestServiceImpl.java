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

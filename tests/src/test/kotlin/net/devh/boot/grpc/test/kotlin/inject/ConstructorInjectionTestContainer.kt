package net.devh.boot.grpc.test.kotlin.inject

import io.grpc.Channel
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.test.inject.CustomGrpc
import net.devh.boot.grpc.test.proto.TestServiceGrpc
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub
import org.springframework.stereotype.Component

/**
 * A client stubs bean container for testing constructor inject
 */
@Component
class ConstructorInjectionTestContainer(

    @GrpcClient("test")
    val channel: Channel,

    @GrpcClient("test")
    val stub: TestServiceGrpc.TestServiceStub,

    @GrpcClient("test")
    val blockingStub: TestServiceBlockingStub,

    @GrpcClient("test")
    val futureStubForClientTest: TestServiceGrpc.TestServiceFutureStub,

    @GrpcClient("anotherTest")
    val anotherBlockingStub: TestServiceBlockingStub,

    @GrpcClient("unnamed")
    val unnamedTestServiceBlockingStub: TestServiceBlockingStub,

    @GrpcClient("test")
    val anotherServiceClientBean: CustomGrpc.FactoryMethodAccessibleStub,
)
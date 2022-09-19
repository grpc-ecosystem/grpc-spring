package net.devh.boot.grpc.test.kotlin.inject

import io.grpc.Channel
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.test.inject.CustomGrpc
import net.devh.boot.grpc.test.proto.TestServiceGrpc
import org.springframework.stereotype.Component

/**
 * A client stubs bean container for testing constructor inject
 */
@Component
class InjectionTestContainer {

    @GrpcClient("test")
    lateinit var channel: Channel

    @GrpcClient("test")
    lateinit var stub: TestServiceGrpc.TestServiceStub

    @GrpcClient("test")
    lateinit var blockingStub: TestServiceGrpc.TestServiceBlockingStub

    @GrpcClient("test")
    lateinit var futureStubForClientTest: TestServiceGrpc.TestServiceFutureStub

    @GrpcClient("anotherTest")
    lateinit var anotherBlockingStub: TestServiceGrpc.TestServiceBlockingStub

    @GrpcClient("unnamed")
    lateinit var unnamedTestServiceBlockingStub: TestServiceGrpc.TestServiceBlockingStub

    @GrpcClient("test")
    lateinit var anotherServiceClientBean: CustomGrpc.FactoryMethodAccessibleStub
}
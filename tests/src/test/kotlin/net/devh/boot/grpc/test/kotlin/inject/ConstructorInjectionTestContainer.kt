package net.devh.boot.grpc.test.kotlin.inject

import io.grpc.Channel
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.test.inject.CustomGrpc
import net.devh.boot.grpc.test.proto.TestServiceGrpc.*
import org.springframework.stereotype.Component

/**
 * A client stubs bean container for testing constructor inject
 */
@Component
class ConstructorInjectionTestContainer(

    @GrpcClient("test")
    val channel: Channel,

    @GrpcClient("test")
    val stub: TestServiceStub,

    @GrpcClient("test", beanName = "blockingStub")
    val blockingStub: TestServiceBlockingStub,

    @GrpcClient("test", beanName = "futureStubForClientTest")
    val futureStubForClientTest: TestServiceFutureStub,

    @GrpcClient("anotherTest", beanName = "anotherBlockingStub")
    val anotherBlockingStub: TestServiceBlockingStub,

    @GrpcClient("unnamed")
    val unnamedTestServiceBlockingStub: TestServiceBlockingStub,

    @GrpcClient("test", beanName = "anotherServiceClientBean")
    val anotherServiceClientBean: CustomGrpc.FactoryMethodAccessibleStub
)
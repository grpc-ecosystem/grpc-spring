package net.devh.boot.grpc.test.kotlin.inject

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.test.proto.TestServiceGrpc
import org.springframework.stereotype.Component

/**
 * Another client stubs bean container for testing constructor inject.
 * Test multiple @GrpcClient with same configs among project wouldn't affect registering of client stub beans.
 */
@Component
class ConstructorInjectionAnotherTestContainer(
    @GrpcClient("test")
    val blockingStub: TestServiceGrpc.TestServiceBlockingStub,

    @GrpcClient("unnamed")
    val unnamedTestServiceBlockingStub: TestServiceGrpc.TestServiceBlockingStub,
)
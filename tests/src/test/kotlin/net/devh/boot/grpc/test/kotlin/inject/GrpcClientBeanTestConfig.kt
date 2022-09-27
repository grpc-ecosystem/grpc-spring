package net.devh.boot.grpc.test.kotlin.inject

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import net.devh.boot.grpc.test.inject.CustomGrpc
import net.devh.boot.grpc.test.proto.TestServiceGrpc
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@GrpcClientBean(
    clazz = TestServiceGrpc.TestServiceBlockingStub::class,
    beanName = "blockingStub",
    client = GrpcClient("test")
)
@GrpcClientBean(
    clazz = TestServiceGrpc.TestServiceFutureStub::class,
    beanName = "futureStubForClientTest",
    client = GrpcClient("test")
)
@GrpcClientBean(
    clazz = TestServiceGrpc.TestServiceBlockingStub::class,
    beanName = "anotherBlockingStub",
    client = GrpcClient("anotherTest")
)
@GrpcClientBean(
    clazz = TestServiceGrpc.TestServiceBlockingStub::class,
    client = GrpcClient("unnamed")
)
@GrpcClientBean(
    clazz = CustomGrpc.FactoryMethodAccessibleStub::class,
    beanName = "anotherServiceClientBean",
    client = GrpcClient("test")
)
@TestConfiguration
open class GrpcClientBeanTestConfig {

    @Bean
    @ConditionalOnMissingBean(name = ["aboutMethodInjectedBlockingStubBean"])
    open fun aboutMethodInjectedBlockingStubBean(
        @Qualifier("anotherBlockingStub") blockingStub: TestServiceGrpc.TestServiceBlockingStub
    ): String =
        blockingStub.toString()

}
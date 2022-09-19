package net.devh.boot.grpc.test.kotlin.inject

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import net.devh.boot.grpc.test.proto.TestServiceGrpc
import org.springframework.context.annotation.Configuration

@Configuration
@GrpcClientBean(
    clazz = TestServiceGrpc.TestServiceBlockingStub::class,
    beanName = "stubFromSpringConfiguration",
    client = GrpcClient("test2")
)
open class GrpcClientBeanCustomConfig
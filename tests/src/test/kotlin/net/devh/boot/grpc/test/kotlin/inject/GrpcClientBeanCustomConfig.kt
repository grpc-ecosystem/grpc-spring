package net.devh.boot.grpc.test.kotlin.inject

import io.grpc.stub.AbstractStub
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import net.devh.boot.grpc.client.stubfactory.StandardJavaGrpcStubFactory
import net.devh.boot.grpc.client.stubfactory.StubFactory
import net.devh.boot.grpc.test.inject.CustomStub
import net.devh.boot.grpc.test.proto.TestServiceGrpc
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@GrpcClientBean(
    clazz = TestServiceGrpc.TestServiceBlockingStub::class,
    beanName = "stubFromSpringConfiguration",
    client = GrpcClient("test2")
)
@TestConfiguration
open class GrpcClientBeanCustomConfig{

    @Bean
    @ConditionalOnMissingBean(name = ["customStubFactory"])
    open fun customStubFactory(): StubFactory =
        object : StandardJavaGrpcStubFactory() {

            override fun isApplicable(stubType: Class<out AbstractStub<*>>): Boolean =
                CustomStub::class.java.isAssignableFrom(stubType)

            override fun getFactoryMethodName(): String =
                "custom"
        }
}
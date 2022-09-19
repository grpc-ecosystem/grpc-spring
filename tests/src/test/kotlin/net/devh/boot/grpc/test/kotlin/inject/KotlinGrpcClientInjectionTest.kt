package net.devh.boot.grpc.test.kotlin.inject

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.test.config.BaseAutoConfiguration
import net.devh.boot.grpc.test.config.InProcessConfiguration
import net.devh.boot.grpc.test.config.ServiceConfiguration
import net.devh.boot.grpc.test.proto.TestServiceGrpc
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import kotlin.test.assertNotNull

@Component
class KotlinConstructorInjectBean(
    @GrpcClient("test", beanName = "blockingStub")
    val blockingStub: TestServiceGrpc.TestServiceBlockingStub,

    @GrpcClient("test")
    val futureStubForClientTest: TestServiceGrpc.TestServiceFutureStub,

    @GrpcClient("anotherTest", beanName = "anotherBlockingStub")
    val anotherBlockingStub: TestServiceGrpc.TestServiceBlockingStub,

    @GrpcClient("unnamed")
    val unnamedTestServiceBlockingStub: TestServiceGrpc.TestServiceBlockingStub,
)

@Component
class KotlinConstructorInjectAnotherBean(
    @GrpcClient("test", beanName = "blockingStub")
    val blockingStub: TestServiceGrpc.TestServiceBlockingStub,

    @GrpcClient("unnamed")
    val unnamedTestServiceBlockingStub: TestServiceGrpc.TestServiceBlockingStub,
)

@SpringBootTest()
@SpringJUnitConfig(
    classes = [
        KotlinConstructorInjectBean::class,
        KotlinConstructorInjectAnotherBean::class,
        InProcessConfiguration::class,
        ServiceConfiguration::class,
        BaseAutoConfiguration::class,
    ]
)
@DirtiesContext
class KotlinGrpcClientInjectionTest {


    @Autowired
    lateinit var bean: KotlinConstructorInjectBean

    @Autowired
    lateinit var anotherBean: KotlinConstructorInjectAnotherBean

    /**
     * Should trigger no bean
     */
    @Test
    fun testConstructorInject() {
        assertNotNull(bean.blockingStub, "blockingStub")
        assertNotNull(anotherBean.blockingStub, "anotherBeanBlockingStub")
    }
}
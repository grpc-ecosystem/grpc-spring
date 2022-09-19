package net.devh.boot.grpc.test.kotlin.inject

import net.devh.boot.grpc.test.config.BaseAutoConfiguration
import net.devh.boot.grpc.test.config.InProcessConfiguration
import net.devh.boot.grpc.test.config.ServiceConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import kotlin.test.assertNotNull

@SpringBootTest()
@SpringJUnitConfig(
    classes = [
        ConstructorInjectionTestContainer::class,
        ConstructorInjectionAnotherTestContainer::class,
        InProcessConfiguration::class,
        ServiceConfiguration::class,
        BaseAutoConfiguration::class,
    ]
)
@DirtiesContext
class KotlinGrpcClientConstructorInjectionTest {

    @Autowired
    lateinit var bean: ConstructorInjectionTestContainer

    @Autowired
    lateinit var anotherBean: ConstructorInjectionAnotherTestContainer

    /**
     * `@GrpcClient` annotated constructor parameter for Kotlin constructor injection.
     */
    @Test
    fun testConstructorInjection() {
        assert(::bean.isInitialized)
        assert(::anotherBean.isInitialized)
        assertNotNull(bean.blockingStub, "blockingStub")
        assertNotNull(bean.anotherServiceClientBean, "anotherServiceClientBean")
        assertNotNull(anotherBean.blockingStub, "anotherContainerBlockingStub")
    }
}
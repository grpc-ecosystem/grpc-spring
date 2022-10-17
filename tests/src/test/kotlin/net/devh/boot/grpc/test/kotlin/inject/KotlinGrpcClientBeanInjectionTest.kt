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

@SpringBootTest
@SpringJUnitConfig(
    classes = [
        InjectionTestContainer::class,
        GrpcClientBeanTestConfig::class,
        GrpcClientBeanCustomConfig::class,
        InProcessConfiguration::class,
        ServiceConfiguration::class,
        BaseAutoConfiguration::class
    ]
)
@DirtiesContext
class KotlinGrpcClientBeanInjectionTest {

    @Autowired
    lateinit var container: InjectionTestContainer

    /**
     * `@GrpcClientBean` for Kotlin property injection.
     */
    @Test
    fun testGrpcClientBeanPropertyInjection() {
        assert(::container.isInitialized)
        assertNotNull(container.blockingStub)
    }
}
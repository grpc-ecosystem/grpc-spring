package net.devh.boot.grpc.examples

import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.examples.local.client.HelloServiceClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LocalGrpcClientKtApplicationTests {
	@Autowired
	lateinit var client: HelloServiceClient
	@Test
	fun test() {
		runBlocking {
			val name = "John"
			assertThat(client.sendMessage(name)).isEqualTo("Hello $name")
		}
	}
}

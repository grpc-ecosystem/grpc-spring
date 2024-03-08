package net.devh.boot.grpc.examples

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LocalGrpcClientKtApplication

fun main(args: Array<String>) {
	runApplication<LocalGrpcClientKtApplication>(*args)
}

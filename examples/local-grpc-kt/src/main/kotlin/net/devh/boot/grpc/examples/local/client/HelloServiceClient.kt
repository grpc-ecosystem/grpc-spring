package net.devh.boot.grpc.examples.local.client

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.examples.lib.HelloGrpcKt
import net.devh.boot.grpc.examples.lib.HelloRequest
import org.springframework.stereotype.Service

@Service
class HelloServiceClient {
    @GrpcClient("hello")
    private lateinit var stub: HelloGrpcKt.HelloCoroutineStub

    suspend fun sendMessage(name: String): String {
        val response = stub.sayHello(HelloRequest.newBuilder().setName(name).build())
        return response.message
    }
}
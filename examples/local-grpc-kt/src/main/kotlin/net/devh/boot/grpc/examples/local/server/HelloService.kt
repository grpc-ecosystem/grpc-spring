package net.devh.boot.grpc.examples.local.server

import net.devh.boot.grpc.examples.lib.HelloGrpcKt
import net.devh.boot.grpc.examples.lib.HelloReply
import net.devh.boot.grpc.examples.lib.HelloRequest
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class HelloService: HelloGrpcKt.HelloCoroutineImplBase() {
    override suspend fun sayHello(request: HelloRequest): HelloReply {
        return HelloReply.newBuilder().setMessage("Hello ${request.name}").build()
    }
}
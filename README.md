# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)

README: [English](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README.md) | [中文](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README-zh.md)

## Features
Auto-configures and runs the embedded gRPC server with @GrpcService-enabled beans as part of spring-boot application.

Support Spring Cloud(registe services to consul or eureka and fetch gRPC server information)

Support Spring Sleuth to trace application

## Usage

### gRPC server

Annotate your server interface implementation(s) with ``@GrpcService``
````java
@GrpcService(GreeterGrpc.class)
public class GrpcServerService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello =============> " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
````
### gRPC client

Use ``@GrpcClient`` annotation to set Channel
 
````java
@GrpcClient("gRPC server name")
private Channel serverChannel;
````

gRPC request

````java
GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverChannel);
HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName(name).build());
````

## Show case
https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/example

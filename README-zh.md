# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)


README: [English](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README.md) | [中文](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README-zh.md)

## Features
在使用 Spring Boot 的应用可以通过使用 @EnableGrpcServer 、@EnableGrpcClient 注解进行自动配置

支持 Spring Cloud（可以通过 Spring Cloud 进行服务注册并且获取 gRPC server 的信息

支持 Spring Sleuth 进行跟踪应用

## Usage

### gRPC server

实现 Grpc 生成的接口，并使用 ``@GrpcService`` 注解

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

使用 ``@GrpcClient`` 注解去设置 Channel 或者 也可以通过 ``GrpcChannelFactory``中的 ``createChannel`` 得到 Channel
 
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

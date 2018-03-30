# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)

README: [English](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README.md) | [中文](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README-zh.md)

## Features
Auto configuring and run the embedded gRPC server with @GrpcService-enabled beans as part of spring-boot application.

Support Spring Cloud(registe services to consul or eureka and fetch gRPC server information)

Support Spring Sleuth to trace application

Support global and customer gRPC server/client interceptors

Support keepalive

## Usage

### gRPC server

To add a dependency using Maven, use the following:

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>1.4.0.RELEASE</version>
</dependency>
````

To add a dependency using Gradle:

````
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:1.4.0.RELEASE'
}
````

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

To add a dependency using Maven, use the following:

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>1.4.0.RELEASE</version>
</dependency>
````

To add a dependency using Gradle:

````
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:1.4.0.RELEASE'
}
````

Use ``@GrpcClient("gRPC server name")`` annotation or ``grpcChannelFactory.createChannel("gRPC server name")`` to get Channel

````java
@GrpcClient("gRPC server name")
private Channel serverChannel;
````

set gRPC host and port in application.properties, default host is 0.0.0.0 and default port is 9090

````
grpc.server.port=9090
grpc.server.address=0.0.0.0
````

gRPC request

````java
GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverChannel);
HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName(name).build());
````

set gRPC server host and port in application.properties, default host is [127.0.0.1] and default port is [9090]

````
grpc.client.(gRPC server name).host[0]=
grpc.client.(gRPC server name).port[0]=
````

## Version Compatibility with gRPC-java

> Note: The version numbers below are only examples

| Project Version  | gRPC-java Version  |
| ---------------- | ------------------ |
| 1.4.0.RELEASE    | 1.10.0            |
| 1.3.0.RELEASE    | 1.6.1              |
| 1.2.0.RELEASE    | 1.3.0              |
| 1.1.1.RELEASE    | 1.2.0              |
| 1.0.1.RELEASE    | 1.1.2              |
| 1.0.0.RELEASE    | 1.0.3              |

## Show case
https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples

## Credits
- [saturnism](https://github.com/saturnism/spring-boot-starter-grpc)

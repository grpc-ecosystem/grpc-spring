# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)


README: [English](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README.md) | [中文](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README-zh.md)

Java技术交流群：294712648 <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=34ad403ce78380042406f11a122637ea9d66c11ae20f331dff37bc90a4fde939"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Java技术交流群" title="Java技术交流群"></a>

## Features
使用 Spring Boot 的应用进行自动配置，内嵌 gRPC server

支持 Spring Cloud（可以通过 Spring Cloud 进行服务注册并且获取 gRPC server 的信息）

支持 Spring Sleuth 进行应用跟踪

## Usage

### gRPC server

添加依赖如果使用的是Maven

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>1.0.0.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:1.0.0.RELEASE'
}
````

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

设置 gRPC 的 host 跟 port 在application.properties，默认的监听的 host 是 0.0.0.0，默认的 port 是 9090

````
grpc.server.port=
grpc.server.host=
````

### gRPC client

添加依赖如果使用的是Maven

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>1.0.0.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:1.0.0.RELEASE'
}
````

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

设置 gRPC 服务器的 host 跟 port 在application.properties，默认的host是[127.0.0.1]，默认的port是[9090]

````
grpc.client.(gRPC server name).host[0]=
grpc.client.(gRPC server name).port[0]=
````

## Show case
https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/example

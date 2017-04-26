# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.devh/grpc-server-spring-boot-starter/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/net.devh/grpc-server-spring-boot-starter)

README: [English](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README.md) | [中文](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README-zh.md)

Java技术交流群：294712648 <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=34ad403ce78380042406f11a122637ea9d66c11ae20f331dff37bc90a4fde939"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Java技术交流群" title="Java技术交流群"></a>

## 特点
使用 Spring Boot 的应用进行自动配置，内嵌 gRPC server

支持 Spring Cloud（可以通过 Spring Cloud 进行服务注册并且获取 gRPC server 的信息）

支持 Spring Sleuth 进行应用跟踪

支持对于 server、client 分别设置全局拦截器或单个的拦截器

支持 keepalive

## 使用方式

### gRPC 服务端

添加依赖如果使用的是Maven

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>1.1.1.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:1.1.1.RELEASE'
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

### gRPC 客户端

添加依赖如果使用的是Maven

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>1.1.1.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:1.1.1.RELEASE'
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

## gRPC-java 的版本兼容

> 说明: 表格中展示的版本仅仅代表该搭配能一起正常工作，不代表其他版本不能正常

| Project Version  | gRPC-java Version  |
| ---------------- | ------------------ |
| 1.1.1.RELEASE    | 1.2.0              |
| 1.0.1.RELEASE    | 1.1.2              |
| 1.0.0.RELEASE    | 1.0.3              |

## 示例
https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples

## 贡献
- [saturnism](https://github.com/saturnism/spring-boot-starter-grpc)
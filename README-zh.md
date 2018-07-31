# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)

README: [English](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README.md) | [中文](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/README-zh.md)

Java技术交流群：294712648 <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=34ad403ce78380042406f11a122637ea9d66c11ae20f331dff37bc90a4fde939"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Java技术交流群" title="Java技术交流群"></a>

## 特点
使用 Spring Boot 的应用可以进行自动配置，内嵌 gRPC server

支持 Spring Cloud（可以通过 Spring Cloud 进行服务注册并且获取 gRPC server 的信息）

支持 Spring Sleuth 进行应用跟踪

支持对于 server、client 分别设置全局拦截器或单个的拦截器

## 版本
2.x.x.RELEASE 支持 Spring Cloud Finchley。

最新的版本：``2.0.0.RELEASE``

1.x.x.RELEASE 支持 Spring Cloud Edgware 、Dalston、Camden。

最新的版本：``1.4.1.RELEASE``

## 使用方式

### gRPC 服务端

添加依赖如果使用的是Maven

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>2.0.0.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:2.0.0.RELEASE'
}
````

实现 gRPC 生成的接口，并使用 ``@GrpcService`` 注解

````java
@GrpcService(GreeterGrpc.class)
public class GrpcServerService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello ====> " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
````

设置 gRPC 的 host 跟 port 在application.properties，默认的监听的 host 是 0.0.0.0，默认的 port 是 9090

````
grpc.server.port=9090
grpc.server.address=0.0.0.0
````

### gRPC 客户端

添加依赖如果使用的是Maven

````
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>2.0.0.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:2.0.0.RELEASE'
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

## 示例
https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples


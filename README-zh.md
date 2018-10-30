# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)
[![MIT License](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)

README: [English](README.md) | [中文](README-zh.md)

Java技术交流群：294712648 <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=34ad403ce78380042406f11a122637ea9d66c11ae20f331dff37bc90a4fde939"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Java技术交流群" title="Java技术交流群"></a>

## 特点

* 使用 Spring Boot 的应用可以进行自动配置，内嵌 gRPC server

* 使用`@ GrpcClient`自动创建和管理你的``channel``和``stub``

* 支持 Spring Cloud（向Consul或Eureka注册服务并获取gRPC服务器信息）

* 支持 Spring Sleuth 进行链路跟踪

* 支持对于 server、client 分别设置全局拦截器或单个的拦截器

## 版本

2.x.x.RELEASE 支持 Spring Boot 2 & Spring Cloud Finchley。

最新的版本：``2.1.0.RELEASE``

1.x.x.RELEASE 支持 Spring Boot 1 & Spring Cloud Edgware 、Dalston、Camden。

最新的版本：``1.4.1.RELEASE``

**注意:** 此项目也可以在没有Spring-Boot的情况下使用，但这需要一些手动bean配置。

## 使用方式

### gRPC 服务端

添加依赖如果使用的是Maven

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>2.1.0.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````gradle
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:2.1.0.RELEASE'
}
````

实现 gRPC 生成的接口，并使用 ``@GrpcService`` 注解

````java
@GrpcService
public class GrpcServerService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello ==> " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
````

设置 gRPC 的 host 跟 port 在application.properties，默认的监听的 host 是 0.0.0.0，默认的 port 是 9090。其他配置
[配置文件](grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/springboot/autoconfigure/grpc/server
/GrpcServerProperties.java)
通过Spring的property机制可以被修改。在server中使用``grpc.server.`的前缀

#### Properties示例

````properties
grpc.server.port=9090
grpc.server.address=0.0.0.0
````

### gRPC 客户端

添加依赖如果使用的是Maven

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>2.1.0.RELEASE</version>
</dependency>
````

添加依赖如果使用的是Gradle

````gradle
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:2.1.0.RELEASE'
}
````

这里有三种方式去或得一个gRPC server的连接


* 使用 `grpcChannelFactory.createChannel(serverName)` 去创建一个 `Channel`，并创建一个自己的 stub.

  ````java
  @Autowired
  private GrpcChannelFactory grpcChannelFactory;

  private GreeterGrpc.GreeterBlockingStub greeterStub;

  @PostConstruct
  public void init() {
      Channel channel = grpcChannelFactory.createChannel("gRPC server name");
      greeterStub = GreeterGrpc.newBlockingStub(channel);
  }
  ````

* 通过在 `Channel` 类型的字段上加入 `@GrpcClient(serverName)` 注解，并创建一个自己的 grpc stub.
  * 不需要使用 `@Autowired` 或者 `@Inject` 来进行注入
  
  ````java
  @GrpcClient("gRPC server name")
  private Channel channel;

  private GreeterGrpc.GreeterBlockingStub greeterStub;

  @PostConstruct
  public void init() {
      greeterStub = GreeterGrpc.newBlockingStub(channel);
  }
  ````
  
* 直接将 `@GrpcClient(serverName)` 注解加在你自己的 stub 上
  * 不需要使用 `@Autowired` 或者 `@Inject` 来进行注入

  ````java
  @GrpcClient("gRPC server name")
  private GreeterGrpc.GreeterBlockingStub greeterStub;
  ````
 
**注意:** 你可以为多个 channels 和多个不同的 stubs 使用相同的 serverName (除非他们拦截器不一样). 

然后你可以直接向服务端发起请求，如下:

````java
HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName(name).build());
````

默认情况下client假设服务端的地址是 `127.0.0.1`,端口是`9090`,这些配置 [settings](grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/springboot/autoconfigure/grpc/client/GrpcChannelProperties.java)
都可以通过Spring的property机制进行更改，客户端上需要使用 `grpc.client.(serverName).`的前缀 

#### Properties示例

````properties
grpc.client.(gRPC server name).host[0]=127.0.0.1
grpc.client.(gRPC server name).port[0]=9090
# 或者
grpc.client.myName.host=127.0.0.1
grpc.client.myName.port=9090
````
## 贡献

我们总是欢迎大家为这个项目做出自己的贡献! 贡献时需要参考 [CONTRIBUTING.md] 文档.

## 示例

https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples


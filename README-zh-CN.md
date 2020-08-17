# gRPC Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)
[![MIT License](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)
[![Crowdin](https://badges.crowdin.net/grpc-spring-boot-starter/localized.svg)](https://crowdin.com/project/grpc-spring-boot-starter)

[![Client-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-client-spring-boot-autoconfigure.svg?label=Client-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-client-spring-boot-autoconfigure)
[![Server-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-server-spring-boot-autoconfigure.svg?label=Server-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-server-spring-boot-autoconfigure)
[![Common-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-common-spring-boot.svg?label=Common-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-common-spring-boot)

README: [English](README.md) | [中文](README-zh-CN.md)

**文档:** [English](https://yidongnan.github.io/grpc-spring-boot-starter/en/) | [中文](https://yidongnan.github.io/grpc-spring-boot-starter/zh-CN/)

QQ交流群：294712648

## 特性

* 在 spring boot 应用中，通过 `@GrpcService` 自动配置并运行一个嵌入式的  gRPC 服务。

* 使用  `@GrpcClient` 自动创建和管理您的 gRPC Channels 和 stubs

* 支持[Spring Cloud](https://spring.io/projects/spring-cloud) (向 [Consul](https://github.com/spring-cloud/spring-cloud-consul) 或 [Eureka](https://github.com/spring-cloud/spring-cloud-netflix) 或 [Nacos](https://github.com/spring-cloud-incubator/spring-cloud-alibaba) 注册服务并获取 gRPC  服务端信息)

* 支持[Spring Sleuth](https://github.com/spring-cloud/spring-cloud-sleuth)作为分布式链路跟踪解决方案(如果[brave-instrument-grpc](https://mvnrepository.com/artifact/io.zipkin.brave/brave-instrumentation-grpc)存在)

* 支持全局和自定义的 gRPC 服务端/客户端拦截器

* 支持 [Spring-Security](https://github.com/spring-projects/spring-security)

* 支持metric (基于[micrometer](https://micrometer.io/)/[actuator](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator) )

* 也适用于 (non-shaded) grpc-netty

## 版本

2.x.x.RELEASE 支持 Spring Boot 2.1.x/2.2.x 和 Spring Cloud Greenwich / Hoxton。

最新版本： `2.10.0.RELEASE`

( `2.4.0.RELEASE` 用于 Spring Boot 2.0.x & Spring Cloud Finchy).

1.x.x.RELEASE 支持 Spring Boot 1 & Spring Cloud Edgware, Dalston, Camden.

最新版本： `1.4.2.RELEASE`

**注意:** 该项目也可以在没有 Spring-Boot 的情况下使用，但是您需要手动配置一些 bean。

## 用法

### gRPC 服务端 + 客户端

使用一下命令添加 Maven 依赖项：

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-spring-boot-starter</artifactId>
  <version>2.10.0.RELEASE</version>
</dependency>
````

Gradle：

````gradle
dependencies {
  compile 'net.devh:grpc-spring-boot-starter:2.10.0.RELEASE'
}
````

### gRPC 服务端

使用一下命令添加 Maven 依赖项：

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>2.10.0.RELEASE</version>
</dependency>
````

Gradle：

````gradle
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:2.10.0.RELEASE'
}
````

在服务端接口实现类上添加 `@GrpcService` 注解。

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

默认情况下，Grpc 服务器将监听端口 `9090`。 端口的配置和其他的 [设置](grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/server/config/GrpcServerProperties.java) 可以通过 Spring 的属性机制进行更改。 服务端的配置使用 `grpc.server.` 前缀。

详情请参阅我们的[文档](https://yidongnan.github.io/grpc-spring-boot-starter/)。

### gRPC 客户端

使用一下命令添加 Maven 依赖项：

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>2.10.0.RELEASE</version>
</dependency>
````

Gradle：

````gradle
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:2.10.0.RELEASE'
}
````
在 grpc 客户端的的 stub 字段上添加 `@GrpcClient(serverName)` 注解。

* 请不要将 @GrpcClient 与 `@Autowireed` 或 `@Inject` 一起使用。

  ````java
  @GrpcClient("gRPC server name")
  private GreeterGrpc.GreeterBlockingStub greeterStub;
  ````

**注意:** 你可以将相同的 grpc 服务端名称用于多个 channel， 也可以使用不同的 stub （甚至使用不同的 stub 拦截器）

然后您可以向您的服务器发送查询，就像这样：

````java
HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName(name).build());
````

可以单独配置每个客户端的目标地址。 但在某些情况下，您可以仅依靠默认配置。 您可以通过 `NameResolver.Factory` Bean 类自定义默认的 url 映射。 如果您没有配置那个Bean，那么默认的 uri 将使用默认方案和名称(如：`dns:<name>`)：

这些配置和其他的 [设置](grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/client/config/GrpcChannelProperties.java) 可以通过 Spring 的属性机制进行更改。 客户端使用`grpc.client.(serverName)。` 前缀。

详情请参阅我们的[文档](https://yidongnan.github.io/grpc-spring-boot-starter/)。

## 使用 (non-shaded) grpc-netty 运行

这个库支持`grpc-netty`和`grpc-nety-shaded`。 后一种可能会防止与不兼容的 gRPC 版本冲突或不同 netty 版本之间的冲突。

**注意:** 如果在classpath 中存在 shaded netty， 则 shaded netty 将使用有线与 non-shaded grpc-netty。

您可以在 Maven 中这样使用。

````xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty</artifactId>
    <version>${grpcVersion}</version>
</dependency>

<!-- For both -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- For the server (only) -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-server-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- For the client (only) -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
        </exclusion>
    </exclusions>
</dependency>
````

Gradle：

````groovy
compile "io.grpc:grpc-netty:${grpcVersion}"

compile 'net.devh:grpc-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For both
compile 'net.devh:grpc-client-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For the client (only)
compile 'net.devh:grpc-server-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For the server (only)
````

## 示例项目

在 [这里](examples)可以阅读更多关于我们的示例项目。

## 排除故障

请参阅我们的[文档](https://yidongnan.github.io/grpc-spring-boot-starter/en/trouble-shooting)寻求帮助。

## 参与贡献

我们始终欢迎您对项目作出任何贡献。 详见[CONTRIBUTING.md](CONTRIBUTING.md)。

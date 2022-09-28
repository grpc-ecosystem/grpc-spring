# 支持 Brave / Sleuth

[<- Back to Index](index.md)

此页面将着重介绍与 [Brave](https://github.com/openzipkin/brave) / [Sleuth](https://spring.io/projects/spring-cloud-sleuth) 的集成。 这是一个可选的功能。

## Table of Contents <!-- omit in toc -->

- [依赖项](#dependencies)
  - [Brave](#brave)
  - [Spring Cloud Sleuth](#spring-cloud-sleuth)
- [关闭链路跟踪](#opt-out)
- [附加信息](#additional-notes)

## 依赖项

grpc-spring-boot-starter 支持为 `Brave Instrumentation：GRPC` 提供自动配置。 然而，有两个要求：

1. 您需要在 classpath 添加 Brave 依赖项。
2. 您需要在应用上下文中有一个 `Trace` bean。 *如果您的 classpath 有 Spring Cloud Sleuth，它将自动为您配置此 Bean*

### Brave

您可以在 Maven 中添加 Brave 的依赖项。

````xml
<dependency>
    <groupId>io.zipkin.brave</groupId>
    <artifactId>brave-instrumentation-grpc</artifactId>
</dependency>
````

Gradle：

````groovy
compile("io.zipkin.brave:brave-instrumentation-grpc")
````

### Spring Cloud Sleuth

您可以在 Maven 中添加 Sleuth 依赖

````xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
````

Gradle：

````groovy
compile("org.springframework.cloud:spring-cloud-starter-sleuth")
````

请参阅[官方文件](https://spring.io/projects/spring-cloud-sleuth)以了解如何设置 / 配置 Sleuth。

## 关闭链路跟踪

您可以使用以下属性关闭 grpc 的链路跟踪：

````property
spring.sleuth.grpc.enabled=false
````

## 附加信息

Spring-Cloud-Sleuth 提供了一些类，例如[`SpringAwareManagedChannelBuilder`](https://javadoc.io/page/org.springframework.cloud/spring-cloud-sleuth-core/latest/org/springframework/cloud/sleuth/instrument/grpc/SpringAwareManagedChannelBuilder.html)，这些类仅仅由于与其他的库兼容而存在。 不要跟那个项目一期使用。 grpc-spring-boot-starter 通过 [`GrpcChannelFactory`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/channelfactory/GrpcChannelFactory.html) 和相关类提供相同 / 扩展的功能提供了开箱即用的能力。 相关阅读 [sleuth's javadoc note](https://github.com/spring-cloud/spring-cloud-sleuth/blob/59216c32f7848ec337fb68d1dbec8e87eeb6bf59/spring-cloud-sleuth-core/src/main/java/org/springframework/cloud/sleuth/instrument/grpc/SpringAwareManagedChannelBuilder.java#L31-L34)。

----------

[<- Back to Index](index.md)

# 支持 Spring Boot Actuator

[<- 返回索引](index.md)

此页面重点介绍与 [Spring-Boot-Actator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html) 的集成。 这是一个可选的功能。 支持的特性

- 客户端 + 服务端指标
- 服务端的`InfoContributor`

## 目录 <!-- omit in toc -->

- [依赖项](#dependencies)
- [指标](#metrics)
  - [计数器](#counter)
  - [计时器](#timer)
  - [查看指标](#viewing-the-metrics)
  - [指标配置](#metric-configuration)
- [InfoContributor](#infocontributor)
- [关闭指标功能](#opt-out)

## 依赖项

指标收集和其他执行器一样都是可选的，如果应用程序环境中有 `MeterRegistry` ，它们将自动启用。

您可以简单地通过向Maven添加以下依赖来实现这一点：

````xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
````

Gradle：

````groovy
compile("org.springframework.boot:spring-boot-starter-actuator")
````

> **注意:** 在大多数情况下，您还需要 `spring-boot-web` 依赖才能实际查看到指标。 请注意，spring-boot-web 运行的端口于不同的 grpc 服务端 (通常是 `8080`)。 如果您不想添加添加一个 web-server，您仍然可以通过 JMX (如果启用的话) 访问这些指标。

## 指标

一旦依赖关系被添加，grpc-spring-boot-starter 将自动配置`ClientIntercertor` / `ServerInterceptor` 以收集指标。

### 计数器

- `grpc.client.requests.sent`: 发送的总请求数。
- `grpc.client.responses.received`:  接受的总响应数。
- `grpc.server.requests.received`: 收到的总请求数。
- `grpc.server.responses.sent`: 发送的总响应数。

**标签**

- `service`: 请求的 grpc 服务名称（使用 protubuf 名称）
- `method`: 请求的 grpc 方法名称（使用 protobuf 名称）
- `methodType`: 请求的 grpc 方法的类型。

### 计时器

- `grpc.client.processing.duration`: 客户端完成请求所花费的总时间，包括网络延迟。
- `grpc.server.processing.duration`: 服务端完成请求所花费的时间。

**标签**

- `service`: 请求的 grpc 服务名称（使用 protobuf 名称）
- `method`: 请求的 grpc 方法名称（使用 protobuf 名称）
- `methodType`: 请求的 grpc 方法的类型。
- `statusCode`: 响应的 `Status.Code`

### 查看指标

您可以在 `/actorator/metrics` (需要一个web-server) 或通过 JMX 查看 grpc 的指标以及其他指标。

> **注意:** 你可能需要先启用指标。
>
> ````properties management.endpoints.web.exposure.include=metrics
>
> # management.endpoints.jmx.exposure.include=metrics
>
> management.endpoint.metrics.enabled=true ````

阅读官方文档以了解更多关于[Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html) 的信息。

### 指标配置

默认情况下，客户端只会为已进行的请求创建指标。 然而，服务端将尝试所有找到并注册的服务，来初始化它们的指标。

您可以通过覆盖Bean的创建自定义的行为。 下面使用`MetricCollectingClientInterceptor`来展示这一点：

````java
@Bean
MetricCollectingClientInterceptor metricCollectingClientInterceptor(MeterRegistry registry) {
    MetricCollectingClientInterceptor collector = new MetricCollectingClientInterceptor(registry,
            counter -> counter.tag("app", "myApp"), // Customize the Counters
            timer -> timer.tag("app", "myApp"), // Customize the Timers
            Code.OK, Code.INVALID_ARGUMENT, Code.UNAUTHENTICATED); // Eagerly initialized status codes
    // Pre-generate metrics for some services (to avoid missing metrics after restarts)
    collector.preregisterService(MyServiceGrpc.getServiceDescriptor());
    return collector;
}
````

## InfoContributor

*仅限服务器*

服务端会自动配置一个 [`InfoContributor`](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/info/InfoContributor.html) 并公开一下信息：

- `grpc.server`:
  - `port`: grpc 服务端的端口
  - `services`:  grpc 的服务列表
    - 方法

您可以在 `/actorator/info` (需要一个web-server) 或通过 JMX 查看 grpc 的信息以及其他信息。

> **注意:** 你可能需要先启用信息。
>
> ````properties management.endpoints.web.exposure.include=info
>
> # management.endpoints.jmx.exposure.include=info
>
> management.endpoint.info.enabled=true ````

您可以使用 `grpc.server.reflectionServiceEnabled=false` 来打开服务列表(对于 actuator 和 grpc)。

## 关闭指标功能

您可以选择退出自动配置，使用以下注解：

````java
@EnableAutoConfiguration(exclude = {GrpcClientMetricAutoConfiguration.class, GrpcServerMetricAutoConfiguration.class})
````

或使用配置：

````properties
spring.autoconfigure.exclude=\
net.devh.boot.grpc.client.autoconfigure.GrpcClientMetricAutoConfiguration,\
net.devh.boot.grpc.server.autoconfigure.GrpcServerMetricAutoConfiguration
````

----------

[<- 返回索引](index.md)

# 配置

[<- 返回索引](../index.md)

本节描述您如何配置您的 grpc-spring-boot-starter 客户端。

## 目录 <!-- omit in toc -->

- [通过属性配置](#configuration-via-properties)
  - [选择目标](#choosing-the-target)
- [通过Beans 配置](#configuration-via-beans)
  - [GrpcChannelConfigurer](#grpcchannelconfigurer)
  - [StubTransformer](#stubtransformer)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- *配置*
- [安全性](security.md)

## 通过属性配置

grpc-spring-boot-starter 可以通过 Spring 的 `@ConfigurationProperties` 机制来进行 [配置](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)

您可以在这里找到所有内建配置属性：

- [`GrpcChannelsProperties`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/config/GrpcChannelsProperties.html)
- [`GrpcChannelProperties`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/config/GrpcChannelProperties.html)
- [`GrpcServerProperties.Security`](https://static.javadoc.io/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/config/GrpcChannelProperties.Security.html)

如果你希望阅读源代码，你可以查阅 [这里](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/client/config/GrpcChannelProperties.java#L58)。

Channels 的属性都是以 `grpc.client.__name__.` 或 `grpc.client.__name__.security.` 为前缀。 Channel 的名称从 `@GrpcClient` 注解中获取。 如果您想要配置一些其他的选项，如为所有服务端设置可信证书，并可以使用 `GLOBAL` 作为名称。 单个 channel 的属性配置会覆盖全局配置。

### 选择目标

您可以使用以下属性指定目标服务器：

````properties
grpc.client.__name__.address=static://localhost:9090
````

目标服务器支持多种 schemes，您可以使用它们来指定目标服务器（优先级0（低） - 10（高））：

- `static`（优先级 4）: 一个简单的IP静态列表 （v4 和 v6）, 可用于连接到服务端 （支持 `localhost`）。 例如：`static://192.168.1:8080,10.0.0.1:1337`
- [`dns`](https://github.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/internal/DnsNameResolver.java#L66)（优先级 5）：解析并绑定到给定 DNS 名称的所有地址。 地址将被缓存，只有当现有连接被关闭 / 失败时才会刷新。 更多选项，例如 `SVC` 查找（对 kubernetes 有用），可以通过系统属性启用。 例如：`dns:///example.my.company`
- `discovery` (优先级 6)：(可选) 使用 Spring Cloud 的`DiscoveryClient` 去查找合适的目标。 在 `HeartbeatEvent` 的时候，连接将自动刷新。 使用 `gRPC_port` 元数据来确定端口，否则使用服务端口。 示例： `discovery:///service-name`
- `self`（优先级 0）：如果您使用 `grpc-server-spring-boot-starter` 并且不想指定自己的地址 / 端口，则可以使用 self 关键词作为 scheme 或者 address 。 这对于需要使用随机服务器端口以避免冲突的测试特别有用。 例如：`self`或`self:self`
- `in-process`：这是一个特殊的方案，将使用 `InProcessChannelFactory` 来替代原有正常的 ChannelFactory。 并使用它连接到 [`InProcessServer`](../server/configuration.md#enabling-the-inprocessserver)。 例如：`in-process:foobar`
- *custom*: 您可以通过 Java 的 `ServiceLoader` 或从 Spring 的应用上下文中选择要自定义的 [`NameResolverProvider`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolverProvider.html) ，并将其注册到 `NameResolverRegistry` 上.

如果您没有定义地址，它将按照如下方式被猜测：

- 首先它将尝试使用它的名称 (`<name>`)
- 如果您配置了默认方案，它将尝试下一个 (`<scheme>:<name>`)
- 然后它将使用 `NameResolver.Factory` 委托的默认方案(见上面的优先级)

> **注意:** 斜杠的数量很重要！ 还要确保不要连接到普通的 web / REST / 非 grpc 服务器（端口）。

[客户端安全性](security.md) 页面上解释了 `SSL` / `TLS` 和其他与安全相关的配置。

## 通过Beans 配置

虽然这个项目提供大多数功能作为配置选项，但有时会因为添加它的开销太高了，我们会选择没有添加它。 如果您觉得这是一项重要功能，请随意打开一项功能性 Pull Request。

如果您要更改应用程序，而不是通过属性进行更改，则可以使用该项目中现有存在的扩展点。

首先，大多数 bean 可以被自定义 bean 替换，您可以按照您想要的任何方式进行配置。 如果您不希望这么麻烦，可以使用 `GrpcChannelConfigurer` 和 `StubTransformer` 等类来配置 channels，stubs 和其他组件，它不会丢失这个项目所提供的任何功能。

### GrpcChannelConfigurer

gRPC 客户端配置器允许您将自定义配置添加到 gRPC 的 `ManagedChannelBuilder` 。

````java
@Bean
public GrpcChannelConfigurer keepAliveClientConfigurer() {
    return (channelBuilder, name) -> {
        if (channelBuilder instanceof NettyChannelBuilder) {
            ((NettyChannelBuilder) channelBuilder)
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(5, TimeUnit.SECONDS);
        }
    };
}
````

> 注意，根据您的配置，在应用上下文中可能有不同类型的 `ManagedChannelBuilder` (例如`InProcessChannelFactory`)。

### StubTransformer

StubTransformer 允许您在注入您的 Bean 之前修改`Stub`。

````java
@Bean
public StubTransformer call() {
    return (name, stub) -> {
        if ("serviceA".equals(name)) {
            return stub.withWaitForReady();
        } else {
            return stub;
        }
    };
}
````

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- *配置*
- [安全性](security.md)

----------

[<- 返回索引](../index.md)

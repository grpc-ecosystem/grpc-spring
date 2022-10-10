# 配置

[<- 返回索引](../index.md)

本节描述您如何配置您的 grpc-spring-boot-starter 应用程序。

## 目录 <!-- omit in toc -->

- [通过属性配置](#通过属性配置)
  - [更改服务端端口](#更改服务端端口)
  - [启用 InProcessServer](#启用 InProcessServer)
- [通过 Bean 配置](#通过 Bean 配置)
  - [ServerInterceptor](#serverInterceptor)
  - [GrpcServerConfigurer](#grpcServerConfigurer)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- *配置*
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [服务端事件](events.md)
- [安全性](security.md)

## 通过属性配置

grpc-spring-boot-starter 可以通过 Spring 的 `@ConfigurationProperties` 机制来进行 [配置](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)。

您可以在这里找到所有内置配置属性：

- [`GrpcServerProperties`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/config/GrpcServerProperties.html)
- [`GrpcServerProperties.Security`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/config/GrpcServerProperties.Security.html)

如果你希望阅读源代码，你可以查阅 [这里](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/server/config/GrpcServerProperties.java#L50)。

Channels 的属性都是以 `grpc.server..` 或 `grpc.client..security.` 为前缀。

### 更改服务端端口

如果您想要将 gRPC 服务端端口从默认值(`9090`) 更改为其他的端口，您可以这样做：

````properties
grpc.server.port=80
````

将端口设置为 `0` 以使用空闲的随机端口。 此功能用于部署了服务发现的服务和并行测试的场景。

> 请确保您不会与其他应用程序或其他端点发生冲突，如`spring-web`。

[服务端安全性](security.md) 页面上解释了 `SSL` / `TLS` 和其他与安全相关的配置。

### 启用 InProcessServer

有时，您可能想要在自己的应用程序中调用自己的 grpc 服务。 您可以像调用其他任何 gRPC 服务端一样，您需要使用 grpc 的 `InProcessServer` 来节省网络间开销。

您可以使用以下属性将其打开：

````properties
grpc.server.in-process-name=<SomeName>
# Optional: Turn off the external grpc-server
#grpc.server.port=-1
````

这允许客户端在同一应用程序内使用以下配置连接到服务器：

````properties
grpc.client.inProcess.address=in-process:<SomeName>
````

这对测试特别有用，因为他们不需要打开特定的端口，因此可以并发运行(在构建 服务器上)。

### 使用 Unix's Domain Sockets

在基于 Unix 的系统上，您也可以使用域套接字在服务器和客户端之间进行本地通信。

只需配置地址即可：

````properties
grpc.server.address=unix:/run/grpc-server
````

然后，客户端可以使用同一地址连接到服务器。

如果您在使用 `grpc-netty` ，您还需要 `nety-transport-native-epoll` 依赖性。 `grpc-netty-shaded` 已经包含了这种依赖性，所以不需要添加任何东西就能正常工作。

## 通过Beans 配置

虽然这个项目为大多数功能提供了配置选项，但有时会因为添加它的成本太高了，我们会选择没有添加它。 如果您觉得这是一项重要功能，请随时提出一个特性请求。

如果您想更改应用程序，并超出了通过属性操作的范围，则可以使用该项目中现有存在的扩展点。

首先，大多数 bean 可以被自定义 bean 替换，您可以按照您想要的任何方式进行配置。 如果您不希望这么麻烦，可以使用 `GrpcServerConfigurer` 来配置你的服务端和其他组件，它不会丢失这个项目所提供的任何功能。

### ServerInterceptor

向您的服务端添加 ` ServerInterceptor` 的三种方式。

- 使用 `@GrpcGlobalServerIntercetor` 注解或者使用 `GlobalServerIntercetorConfigurer` 将 `ServerInterceptor` 定义为全局拦截器
- 在 `@GrpcService#interceptors` 或 `@GrpcService#interceptorNames` 字段中明确列出
- 使用 ` GrpcServerConfigurer` 并调用 `serverBuilder.intercept(ServerInterceptor interceptor)` 方法

### GrpcServerConfigurer

Grpc 服务端配置器允许您将自定义配置添加到 gRPC 的 `ServerBuilder`。

````java
@Bean
public GrpcServerConfigurer keepAliveServerConfigurer() {
    return serverBuilder -> {
        if (serverBuilder instanceof NettyServerBuilder) {
            ((NettyServerBuilder) serverBuilder)
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(5, TimeUnit.SECONDS)
                    .permitKeepAliveWithoutCalls(true);
        }
    };
}
````

> 注意，根据您的配置，在应用程序上下文中可能有不同类型的 `ServerBuilder` (例如`InProcessServerBuilder`)。

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- *配置*
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [服务端事件](events.md)
- [安全性](security.md)

----------

[<- 返回索引](../index.md)

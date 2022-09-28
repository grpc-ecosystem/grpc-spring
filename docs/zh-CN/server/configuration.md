# 配置

[<- Back to Index](../index.md)

本节描述您如何配置您的 grpc-spring-boot-starter 应用程序。

## 目录 <!-- omit in toc -->

- [通过属性配置](#configuration-via-properties)
  - [更改服务端端口](#changing-the-server-port)
  - [启用 InProcessServer](#enabling-the-inprocessserver)
- [通过 Bean 配置](#configuration-via-beans)
  - [ServerInterceptor](#serverinterceptor)
  - [GrpcServerConfigurer](#grpcserverconfigurer)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- *配置*
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Server Events](events.md)
- [Security](security.md)

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

The `SSL`/`TLS` and other security relevant configuration is explained on the [Server Security](security.md) page.

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

### Using Unix's Domain Sockets

On Unix based systems you can also use domain sockets to locally communicate between server and clients.

Simply configure the address like this:

````properties
grpc.server.address=unix:/run/grpc-server
````

Clients can then connect to the server using the same address.

If you are using `grpc-netty` you also need the `netty-transport-native-epoll` dependency. `grpc-netty-shaded` already contains that dependency, so there is no need to add anything for it to work.

## 通过Beans 配置

While this library intents to provide most of the features as configuration option, sometimes the overhead for adding it is too high and thus we didn't add it, yet. If you feel like it is an important feature, feel free to open a feature request.

If you want to change the application beyond what you can do through the properties, then you can use the existing extension points that exist in this library.

First of all most of the beans can be replaced by custom ones, that you can configure in every way you want. If you don't wish to go that far, you can use classes such as `GrpcServerConfigurer` to configure the server and other components without losing the features provided by this library.

### ServerInterceptor

There are three ways to add a `ServerInterceptor` to your server.

- Define the `ServerInterceptor` as a global interceptor using either the `@GrpcGlobalServerInterceptor` annotation, or a `GlobalServerInterceptorConfigurer`
- Explicitly list them in the `@GrpcService#interceptors` or `@GrpcService#interceptorNames` field
- Use a `GrpcServerConfigurer` and call `serverBuilder.intercept(ServerInterceptor interceptor)`

### GrpcServerConfigurer

The grpc server configurer allows you to add your custom configuration to grpc's `ServerBuilder`s.

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

- [Getting Started](getting-started.md)
- *Configuration*
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Server Events](events.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

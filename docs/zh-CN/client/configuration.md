# 配置

[<- 返回索引](../index.md)

本节描述您如何配置您的 grpc-spring-boot-starter 客户端。

## 目录 <!-- omit in toc -->

- [通过属性配置](#通过属性配置)
  - [选择目标](#选择目标)
- [通过 Bean 配置](#通过 Bean 配置)
  - [GrpcClientBean](#grpcClientBean)
  - [GrpcChannelConfigurer](#grpcChannelConfigurer)
  - [ClientInterceptor](#clientInterceptor)
  - [StubFactory](#stubFactory)
  - [StubTransformer](#stubTransformer)
- [自定义 NameResolverProvider](#自定义 NameResolverProvider)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- *配置*
- [安全性](security.md)
- [使用 Grpc-Stubs 测试](testing.md)

## 通过属性配置

grpc-spring-boot-starter 可以通过 Spring 的 `@ConfigurationProperties` 机制来进行 [配置](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)。

您可以在这里找到所有内置配置属性：

- [`GrpcChannelsProperties`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-starter/latest/net/devh/boot/grpc/client/config/GrpcChannelsProperties.html)
- [`GrpcChannelProperties`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-starter/latest/net/devh/boot/grpc/client/config/GrpcChannelProperties.html)
- [`GrpcChannelProperties.Security`](https://static.javadoc.io/net.devh/grpc-client-spring-boot-starter/latest/net/devh/boot/grpc/client/config/GrpcChannelProperties.Security.html)

如果你希望阅读源代码，你可以查阅 [这里](https://github.com/grpc-ecosystem/grpc-spring/blob/master/grpc-client-spring-boot-starter/src/main/java/net/devh/boot/grpc/client/config/GrpcChannelProperties.java#L58)。

Channels 的属性都是以 `grpc.client.__name__.` 或 `grpc.client.__name__.security.` 为前缀。 Channel 的名称从 `@GrpcClient("__name__")` 注解中获取。 如果您想要配置一些其他的选项，如为所有服务端设置可信证书，并可以使用 `GLOBAL` 作为名称。 指定 Channel 的配置项优先于 `GLOBAL` 的配置项

### 选择目标

您可以使用以下属性指定目标服务器：

````properties
grpc.client.__name__.address=static://localhost:9090
````

目标服务器支持多种 schemes，您可以使用它们来指定目标服务器（优先级0（低） - 10（高））：

- `static`（优先级 4）: 一个简单的IP静态列表 （v4 和 v6）, 可用于连接到服务端 （支持 `localhost`）。 若要解析主机名，请使用 `dns` 代替。 例如：`static://192.168.1:8080,10.0.0.1:1337`
- [`dns`](https://github.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/internal/DnsNameResolver.java#L66)（优先级 5）：解析并绑定到给定 DNS 名称的所有地址。 地址将被缓存，只有当现有连接被关闭 / 失败时才会刷新。 示例： `dns:///example.my.company` 注意：grpclb 中包含了一个 `dns` 解析器，并且它的优先级(`6`) 高于默认值， 同时它也支持 `SVC` 查询。 见 [Kubernetes 设置](../kubernetes.md)。
- `discovery` (优先级 6)：(可选) 使用 Spring Cloud 的`DiscoveryClient` 去查找合适的目标。 在 `HeartbeatEvent` 的时候，连接将自动刷新。 使用 `gRPC_port` 元数据来确定端口，否则使用服务端口。 示例： `discovery:///service-name`
- `self`（优先级 0）：如果您使用 `grpc-server-spring-boot-starter` 并且不想指定自己的地址 / 端口，则可以使用 self 关键词作为 scheme 或者 address 。 这对于需要使用随机服务器端口以避免冲突的测试特别有用。 示例： `self:self`
- `in-process`：这是一个特殊的方案，将使用 `InProcessChannelFactory` 来替代原有正常的 ChannelFactory。 并使用它连接到 [`InProcessServer`](../server/configuration.md#enabling-the-inprocessserver)。 例如：`in-process:foobar`
- `unix` (仅适用于基于 Unix 的系统)： 这是一个使用unix域套接字地址连接到服务器的特殊方案。 如果您在使用 `grpc-netty` ，您还需要 `nety-transport-native-epoll` 依赖性。 `grpc-netty-shaded` 已经包含了这种依赖性，所以不需要添加任何东西就能正常工作。 示例： `unix:/run/grpc-server`
- *custom*: 您可以通过 Java 的 `ServiceLoader` 或从 Spring 的应用上下文中选择要自定义的 [`NameResolverProvider`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolverProvider.html) ，并将其注册到 `NameResolverRegistry` 上。 见 [自定义 NameResolverProvider](#custom-nameresolverprovider)

如果您没有定义地址，它将按照如下方式被猜测：

- 如果您已经配置了默认方案，那会先尝试它
  - 如果只提供了一个方案，地址将是 `<scheme>://<name>`。 可选，如果方案需要它，可以选择包含 `:`, `:/`, 或 `://` 。
- 然后它将尝试使用它的名称 (`<name>`)
- 然后它将使用 `NameResolver.Factory` 委托的默认方案(见上面的优先级)

> **注意:** 斜杠的数量很重要！ 还要确保不要连接到普通的 web / REST / 非 grpc 服务器（端口）。

[客户端安全性](security.md) 页面上解释了 `SSL` / `TLS` 和其他与安全相关的配置。

## 通过Beans 配置

虽然这个项目提供大多数功能作为配置选项，但有时会因为添加它的开销太高了，我们会选择没有添加它。 如果您觉得这是一项重要功能，请随意打开一项功能性 Pull Request。

如果您要更改应用程序，而不是通过属性进行更改，则可以使用该项目中现有存在的扩展点。

首先，大多数 bean 可以被自定义 bean 替换，您可以按照您想要的任何方式进行配置。 如果您不希望这么麻烦，可以使用 `GrpcChannelConfigurer` 和 `StubTransformer` 等类来配置 channels，stubs 和其他组件，它不会丢失这个项目所提供的任何功能。

### GrpcClientBean

在您不可以通过 `@GrpcClient` 注入实例时，使用此注解可以注入bean。 这个注解可以重复添加到您的 `@Configuration` 类中。

> **注意：**我们建议在整个应用程序中使用`@GrpcClientBean`或用`@GrpcClient`注释的字段，因为这两者的混合使用可能会给未来的开发者带来混乱。

````java
@Configuration
@GrpcClientBean(
    clazz = TestServiceBlockingStub.class,
    beanName = "blockingStub",
    client = @GrpcClient("test")
)
@GrpcClientBean(
    clazz = FactoryMethodAccessibleStub.class,
    beanName = "accessibleStub",
    client = @GrpcClient("test"))
public class YourCustomConfiguration {

    @Bean
    FooService fooServiceBean(@Autowired TestServiceGrpc.TestServiceBlockingStub blockingStub) {
        return new FooService(blockingStub);
    }

}

@Service
@AllArgsConsturtor
public class BarService {

    private FactoryMethodAccessibleStub accessibleStub;

    public String receiveGreeting(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return accessibleStub.sayHello(request).getMessage();
    }

}
````

### GrpcChannelConfigurer

Grpc 客户端配置器允许您将自定义配置添加到 gRPC 的 `ManagedChannelBuilder` 中 。

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

### ClientInterceptor

`ClientInterceptor` 可以用于各种任务，包括：

- 认证/授权
- 请求校验
- 响应过滤
- 在调用中附加上下文(例如追踪id)
- 错误异常跟 Response `Status` 的映射
- 日志
- ...

向您的 Channel 添加 `ClientInterceptor` 的三种方式。

- 使用 `@GrpcGlobalClientIntercetor` 注解或 `GlobalClientInterceptorConfigurer`将 `ClientInterceptor` 定义为一个全局拦截器
- 在 `@GrpcClient#interceptors` 或 `@GrpcClient#interceptorNames` 字段中明确列出
- 使用 `StubTransformer` 并调用 `stub.withInterceptors(ClientInterceptor... interceptors)`

以下示例演示如何使用注解创建全局客户拦截器：

````java
@Configuration
public class GlobalInterceptorConfiguration {

    @GrpcGlobalClientInterceptor
    LogGrpcInterceptor logClientInterceptor() {
        return new LogGrpcInterceptor();
    }

}
````

下面的示例通过 `GlobalClientInterestorConfigurer` 演示创建全局拦截器

````java
@Configuration
public class GlobalInterceptorConfiguration {

    @Bean
    GlobalClientInterceptorConfigurer globalClientInterceptorConfigurer() {
        interceptors -> interceptors.add(new LogGrpcInterceptor());
    }

}
````

如果您想要将第三方拦截器添加到全局拦截器，上述这些变种会非常简单。

对于您自己的拦截器实现功能，您可以通过添加注解到类本身来实现相同的结果：

````java
@GrpcGlobalClientInterceptor
public class LogGrpcInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogGrpcInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            final MethodDescriptor<ReqT, RespT> method,
            final CallOptions callOptions,
            final Channel next) {

        log.info(method.getFullMethodName());
        return next.newCall(method, callOptions);
    }

}
````

### StubFactory

`StubFactory` 用于创建一个特定类型的 `Stub` 。 注册的 stub 工厂将按顺序检查是否适用，第一个适用的工厂将被用来创建 stub。

该库内置了对 grpc-java 中定义的 `Stub` 类型的支持：

- [`AsyncStubs`](https://grpc.github.io/grpc-java/javadoc/io/grpc/stub/AbstractAsyncStub.html)
- [`BlockingStubs`](https://grpc.github.io/grpc-java/javadoc/io/grpc/stub/AbstractBlockingStub.html)
- [`FutureStubs`](https://grpc.github.io/grpc-java/javadoc/io/grpc/stub/AbstractFutureStub.html)

但是您也可以通过向应用程序上下文添加自定义 `StubFactory` 轻松添加对其他 `Stub` 类型的支持。

````java
@Component
public class MyCustomStubFactory implements StubFactory {

    @Override
    public MyCustomStub<?> createStub(Class<? extends AbstractStub<?>> stubType, Channel channel) {
        try {
            Class<?> enclosingClass = stubType.getEnclosingClass();
            Method factoryMethod = enclosingClass.getMethod("newMyBetterFutureStub", Channel.class);
            return stubType.cast(factoryMethod.invoke(null, channel));
        } catch (Exception e) {
            throw new BeanInstantiationException(stubType, "Failed to create gRPC stub", e);
        }
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractStub<?>> stubType) {
        return AbstractMyCustomStub.class.isAssignableFrom(stubType);
    }

}
````

> **注意：** 请在我们的 issue 中报告缺少支持的 stub 类型(和相应的库)，以便我们添加对它的支持。

### StubTransformer

StubTransformer 允许您在注入您的 Bean 之前修改`Stub`。 如果要往 stub 中添加 `CallCredentials` ，我们会推荐你用这种方式。

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

您也可以使用 `StubTransformer`来添加自定义 `ClientInterceptor` 到你的 stub 中。

> **注意**: `StubTransformer` 是在  `@GrpcClient#interceptors(Names)` 添加后应用的。

## 自定义 NameResolverProvider

有时，你可能想根据一些自定义的逻辑来决定连接到哪个服务器：这时你可以使用自定义 `NameResolverProvider` 来实现。

> **注意：** 这只能用于在应用程序级别上，而不是应用在每个请求级别上。

这个库内置提供了一些 `NameResolverProvider`，因此你可以直接使用 [它们](https://github.com/grpc-ecosystem/grpc-spring/tree/master/grpc-client-spring-boot-starter/src/main/java/net/devh/boot/grpc/client/nameresolver)。

你也利用 Java 的 `ServiceLoader` ，在 `META-INF/services/io.grpc.NameResolverProvider` 文件中添加，或者通过在 spring context 中添加，以此注册自定义的 `NameResolverProvider`。 如果你想在你的 `NameResolver` 中使用一些 spring 的 bean， 那么你必须通过 spring 的 context 来定义它 (否则会使用使用 `static`)。

> **注意：** `NameResolverProvider` 注册是全局的： 如果你在同一JVM中注册了两个或多个，那可能会遇到问题（例如测试期间）。

## HealthIndicator

这个库会自动为所有 `grpcChannel` 客户端提供 `HealthIndicator` (actuator/health)。 如果客户端有 `TRANSIENT_FAILURE` 状态，那服务为上报 `OUT_OF_SERVICE`。 如果您想要去掉它并提供更具体的 `HealthIndicator`， 您可以通过 exclude `GrpcClientHealthAutoConfiguration` 来禁用它。

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- *配置*
- [安全性](security.md)
- [使用 Grpc-Stubs 测试](testing.md)

----------

[<- 返回索引](../index.md)

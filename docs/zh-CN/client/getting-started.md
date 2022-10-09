# 入门指南

[<- 返回索引](../index.md)

本节讨论如何让 Spring 连接到 gRPC 服务端并管理您的连接。

## 目录 <!-- omit in toc -->

- [项目创建](#project-setup)
- [依赖项](#dependencies)
  - [接口项目](#interface-project)
  - [服务端项目](#server-project)
  - [客户端项目](#client-project)
- [使用 Stubs 连接服务端](#using-the-stubs-to-connect-to-the-server)
  - [解释客户端组件](#explaining-the-client-components)
  - [访问客户端](#accessing-the-client)

## 附加主题 <!-- omit in toc -->

- *入门指南*
- [配置](configuration.md)
- [安全性](security.md)
- [使用 Grpc-Stubs 测试](testing.md)

## 项目创建

在我们开始添加依赖关系之前，让我们项目的一些设置建议开始。

![项目创建](/grpc-spring-boot-starter/assets/images/client-project-setup.svg)

我们建议将您的项目分为2至3个不同的模块。

1. **interface 项目** 包含原始 protobuf 文件并生成 java model 和 service 类。 你可能会在不同的项目中会共享这个部分。
2. **Server 项目** 包含项目的业务实现，并使用上面的 Interface 项目作为依赖项。
3. **Client 项目**（可选，可能很多） 任何使用预生成的 stub 来访问服务器的客户端项目。

## 依赖项

### 接口项目

请参阅 [服务端入门指引](../server/getting-started.md#interface-project) 页面

### 服务端项目

请参阅 [服务端入门指引](../server/getting-started.md#server-project) 页面

### 客户端项目

#### Maven (客户端)

````xml
    <dependencies>
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-client-spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>example</groupId>
            <artifactId>my-grpc-interface</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
````

#### Gradle (客户端)

````gradle
apply plugin: 'org.springframework.boot'

dependencies {
    compile('org.springframework.boot:spring-boot-starter')
    compile('net.devh:grpc-client-spring-boot-starter')
    compile('my-example:my-grpc-interface')
}

buildscript {
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
}

````

## 使用 Stubs 连接服务端

本节假定您已经定义并生成了[Protobuf](../server/getting-started.md#creating-the-grpc-service-definitions)。

### 解释客户端组件

以下列表包含您可能在客户端使用到的的所有功能。 如果您不想使用任何高级功能，那么前两个元素可能都是您需要使用的。

- [`@GrpcClient`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/GrpcClient.html): 在需要注入的客户端的字段或者 setter 方法上这个注解。 支持 `Channel`和各种类型的 `Stub`。 请不要将 `@GrpcClient` 与 `@Autowireed` 或 `@Inject` 一起使用。 目前，它不支持构造函数和 `@Bean` 方法参数。 这种情况请查看后面 `@GrpcClientBean` 的使用文档。 **注意：** 同一应用程序提供的服务只能在 ` ApplicationStartedEvent` 之后访问/调用。 连接到外部服务的 Stubs 可以提前使用；从 `@PostConstruct` / `初始化Bean#afterPropertiesSet()` 开始。
- [`@GrpcClientBean`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/GrpcClientBean.html): 注解会把使用 `@GrpcClient` 注解的类型注册到 Spring Context 中，方便 `@Autowired` 和 `@Qualifier` 的使用。 这个注解可以重复添加到您的 `@Configuration` 类中。
- [`Channel`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/Channel.html): Channel 是单个地址的连接池。 目标服务器可能是多个 gRPC 服务。 该地址将使用 `NameResolver` 做解析，最终它可能会指向一批固定数量或动态数量的服务端。
- [`ManagedChannel`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/ManagedChannel.html): ManagedChannel 是 Channel 的一种特殊变体，因为它允许对连接池进行管理操作，例如将其关闭。
- [`NameResolver`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolver.html)、 [`NameResolver.Factory`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolver.Factory.html)： 一个用于将地址解析为`SocketAddress` 列表的类 ，当与先前列出的服务端连表连接失败或通道空闲时，地址将会重新做解析。 参阅 [Configuration -> Choosing the Target](configuration.md#choosing-the-target)。
- [`ClientInterceptor`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/ClientInterceptor.html): 在每个 `Channel` 处理之前拦截它们。 可以用于日志、监测、元数据处理和请求/响应的重写。 grpc-spring-boot-starter 将自动接收所有带有 [`@GrpcGlobalClientInterceptor`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/interceptor/GrpcGlobalClientInterceptor.html) 注解以及手动注册在[`GlobalClientInterceptorRegistry`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/interceptor/GlobalClientInterceptorRegistry.html) 上的客户拦截器。 参阅 [Configuration -> ClientInterceptor](configuration.md#clientinterceptor)。
- [`CallCredentials`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/CallCredentials.html): 管理身份验证的组件。 它可以用于存储凭据和会话令牌。 它还可以用来身份验证，并且使用返回的令牌(例如 OAuth) 来授权实际请求。 除此之外，如果令牌过期并且重新发送请求，它可以续签令牌。 如果您的应用程序上下文中只存在一个 `CallCredentials` bean，那么 spring 将会自动将其附加到`Stub`（ **非** `Channel` ）。 [`CallCredentialsHelper`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/security/CallCredentialsHelper.html)工具类可以帮助您创建常用的 `CallCredentials` 类型和相关的`StubTransformer`。
- [`StubFactory`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/stubfactory/StubFactory.html): 一个用于从 `Channel` 创建特定 `Stub` 的工厂。 可以注册多个 `StubFactory`，以支持不同类型的 stub。 参阅 [Configuration -> StubFactory](configuration.md#stubfactory)。
- [`StubTransformer`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/StubTransformer.html): 所有客户端的 `Stub` 的注入之前应用的转换器。 参阅 [Configuration -> StubTransformer](configuration.md#stubtransformer)。

### 访问客户端

我们建议对 (`@GrpcClient`) `Stub` 做注入，而不是 `Channel`。

> **注意:** 存在不同类型的 `Stub`。 并非所有的都支持所有请求类型 (流式调用)。

````java
import example.HelloRequest;
import example.MyServiceGrpc.MyServiceBlockingStub;

import net.devh.boot.grpc.client.inject.GrpcClient;

import org.springframework.stereotype.Service;

@Service
public class FoobarService {

    @GrpcClient("myService")
    private MyServiceBlockingStub myServiceStub;

    public String receiveGreeting(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return myServiceStub.sayHello(request).getMessage();
    }

}
````

你也可以随时用`@GrpcClientBean`和`@Configuration`注入存根，以便在其他服务中更广泛地使用。

> **注意：**我们建议在整个应用程序中使用`@GrpcClientBean`或用`@GrpcClient`注释的字段，因为这两者的混合使用可能会给未来的开发者带来混乱。

````java
@Configuration
@GrpcClientBean(
    clazz = TestServiceGrpc.TestServiceBlockingStub.class,
    beanName = "blockingStub",
    client = @GrpcClient("test")
)
public class YourCustomConfiguration {

    @Bean
    FoobarService foobarService(@Autowired TestServiceGrpc.TestServiceBlockingStub blockingStub) {
        return new FoobarService(blockingStub);
    }

}

@Service
@AllArgsConsturtor
public class FoobarService {

    private TestServiceBlockingStub blockingStub;

    public String receiveGreeting(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return blockingStub.sayHello(request).getMessage();
    }

}
````

## 附加主题 <!-- omit in toc -->

- *入门指南*
- [配置](configuration.md)
- [安全性](security.md)

----------

[<- 返回索引](../index.md)

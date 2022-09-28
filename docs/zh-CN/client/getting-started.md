# 入门指南

[<- Back to Index](../index.md)

本节讨论如何让 Spring 连接到 gRPC 服务端并管理您的连接。

## 目录 <!-- omit in toc -->

- [项目创建](#project-setup)
- [依赖项](#dependencies)
  - [接口项目](#interface-project)
  - [服务端项目](#server-project)
  - [客户端项目](#client-project)
- [使用 Stubs 连接服务端](#using-the-stubs-to-connect-to-the-server)
  - [Explaining the Client Components](#explaining-the-client-components)
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

See the [server getting started page](../server/getting-started.md#interface-project)

### 服务端项目

See the [server getting started page](../server/getting-started.md#server-project)

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

This section assumes that you have already defined and generated your [Protobuf service](../server/getting-started.md#creating-the-grpc-service-definitions).

### Explaining the Client Components

The following list contains all features that you might encounter on the client side. If you don't wish to use any advanced features, then the first two elements are probably all you need to use.

- [`@GrpcClient`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/GrpcClient.html): The annotation that marks fields and setters for auto injection of clients. Supports `Channel`s, and all kinds of `Stub`s. Do not use `@GrpcClient` in conjunction with `@Autowired` or `@Inject`. Currently, it isn't supported for constructor and `@Bean` method parameters. For this case look below to the `@GrpcClientBean`. \
**Note:** Services provided by the same application can only be accessed/called in/after the `ApplicationStartedEvent`. Stubs connecting to services outside of the application can be used earlier; starting with `@PostConstruct` / `InitializingBean#afterPropertiesSet()`.
- [`@GrpcClientBean`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/GrpcClientBean.html): The annotation helps to register `@GrpcClient` beans in the Spring context to be used with `@Autowired` and `@Qualifier`. The annotation can be repeatedly added to any of your `@Configuration` classes.
- [`Channel`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/Channel.html): The Channel is a connection pool for a single address. The target servers might serve multiple grpc-services though. The address will be resolved using a `NameResolver` and might point to a fixed or dynamic number of servers.
- [`ManagedChannel`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/ManagedChannel.html): The ManagedChannel is a special variant of a Channel as it allows management operations to the connection pool such as shuting it down.
- [`NameResolver`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolver.html) respectively [`NameResolver.Factory`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolver.Factory.html): A class that will be used to resolve the address to a list of `SocketAddress`es, the address will usually be re-resolved when a connection to a previously listed server fails or the channel was idle. See also [Configuration -> Choosing the Target](configuration.md#choosing-the-target).
- [`ClientInterceptor`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/ClientInterceptor.html): Intercepts every call before they are handed to the `Channel`. Can be used for logging, monitoring, metadata handling, and request/response rewriting. grpc-spring-boot-starter will automatically pick up all client interceptors that are annotated with [`@GrpcGlobalClientInterceptor`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/interceptor/GrpcGlobalClientInterceptor.html) or are manually registered to the [`GlobalClientInterceptorRegistry`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/interceptor/GlobalClientInterceptorRegistry.html). See also [Configuration -> ClientInterceptor](configuration.md#clientinterceptor).
- [`CallCredentials`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/CallCredentials.html): A potentially active component that manages the authentication for the calls. It can be used to store credentials or session tokens. It can also be used to authenticate at an authentication provider and then use returned tokens (such as OAuth) to authorize the actual request. In addition to that, it is able to renew the token, if it expired and re-sent the request. If exactly one `CallCredentials` bean is present on your application context then spring will automatically attach it to all `Stub`s (**NOT** `Channel`s). The [`CallCredentialsHelper`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/security/CallCredentialsHelper.html) utility class helps you to create commonly used `CallCredentials` types and related `StubTransformer`.
- [`StubFactory`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/stubfactory/StubFactory.html): A factory that can be used to create a specfic `Stub` type from a `Channel`. Multiple `StubFactory`s can be registered to support different stub types. See also [Configuration -> StubFactory](configuration.md#stubfactory).
- [`StubTransformer`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/StubTransformer.html): A transformer that will be applied to all client `Stub`s before they are injected. See also [Configuration -> StubTransformer](configuration.md#stubtransformer).

### 访问客户端

We recommended to inject (`@GrpcClient`) `Stub`s instead of plain `Channel`s.

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

Also you can feel free to inject stub with `@GrpcClientBean` with `@Configuration` for more wide usage in another services.

> **Note:** We recommend using either `@GrpcClientBean`s or fields annotated with `@GrpcClient` throughout your application, as mixing the two can cause confusion for future developers.

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

[<- Back to Index](../index.md)

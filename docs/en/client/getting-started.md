# Getting Started

[<- Back to Index](../index.md)

This section deals with how to get Spring to connect to a grpc server and manage the connection for you.

## Table of Contents <!-- omit in toc -->

- [Project Setup](#project-setup)
- [Dependencies](#dependencies)
  - [Interface-Project](#interface-project)
  - [Server-Project](#server-project)
  - [Client-Project](#client-project)
- [Using the Stubs to connect to the Server](#using-the-stubs-to-connect-to-the-server)
  - [Explaining the Client Components](#explaining-the-client-components)
  - [Accessing the Client](#accessing-the-client)

## Additional Topics <!-- omit in toc -->

- *Getting Started*
- [Configuration](configuration.md)
- [Security](security.md)
- [Tests with Grpc-Stubs](testing.md)

## Project Setup

Before we start adding the dependencies lets start with some of our recommendation for your project setup.

![project setup](/grpc-spring-boot-starter/assets/images/client-project-setup.svg)

We recommend splitting your project into 2-3 separate modules.

1. **The interface project**
  Contains the raw protobuf files and generates the java model and service classes. You probably share this part.
2. **The server project**
  Contains the actual implementation of your project and uses the interface project as dependency.
3. **The client projects** (optional and possibly many)
  Any client projects that use the pre-generated stubs to access the server.

## Dependencies

### Interface-Project

See the [server getting started page](../server/getting-started.md#interface-project)

### Server-Project

See the [server getting started page](../server/getting-started.md#server-project)

### Client-Project

#### Maven (Client)

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

#### Gradle (Client)

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

## Using the Stubs to connect to the Server

This section assumes that you have already defined and generated your [Protobuf service](../server/getting-started.md#creating-the-grpc-service-definitions).

### Explaining the Client Components

The following list contains all features that you might encounter on the client side.
If you don't wish to use any advanced features, then the first element is probably all you need to use.

- [`@GrpcClient`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/GrpcClient.html):
  The annotation that marks fields and setters for auto injection of clients.
  Supports `Channel`s, and all kinds of `Stub`s.
  Do not use `@GrpcClient` in conjunction with `@Autowired` or `@Inject`.
  Currently it isn't supported for constructor and `@Bean` method parameters. \
  **Note:** Services provided by the same application can only be accessed/called in/after the
  `ApplicationStartedEvent`. Stubs connecting to services outside of the application can be used earlier; starting with
  `@PostConstruct` / `InitializingBean#afterPropertiesSet()`.
- [`Channel`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/Channel.html):
  The Channel is a connection pool for a single address. The target servers might serve multiple grpc-services though.
  The address will be resolved using a `NameResolver` and might point to a fixed or dynamic number of servers.
- [`ManagedChannel`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/ManagedChannel.html):
  The ManagedChannel is a special variant of a Channel as it allows management operations to the connection pool such as
  shuting it down.
- [`NameResolver`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolver.html) respectively
  [`NameResolver.Factory`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolver.Factory.html):
  A class that will be used to resolve the address to a list of `SocketAddress`es, the address will usually be
  re-resolved when a connection to a previously listed server fails or the channel was idle.
  See also [Configuration -> Choosing the Target](configuration.md#choosing-the-target).
- [`ClientInterceptor`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/ClientInterceptor.html):
  Intercepts every call before they are handed to the `Channel`. Can be used for logging, monitoring, metadata handling,
  and request/response rewriting.
  grpc-spring-boot-starter will automatically pick up all client interceptors that are annotated with
  [`@GrpcGlobalClientInterceptor`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/interceptor/GrpcGlobalClientInterceptor.html)
  or are manually registered to the
  [`GlobalClientInterceptorRegistry`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/interceptor/GlobalClientInterceptorRegistry.html).
  See also [Configuration -> ClientInterceptor](configuration.md#clientinterceptor).
- [`CallCredentials`](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/CallCredentials.html):
  A potentially active component that manages the authentication for the calls. It can be used to store credentials or
  session tokens. It can also be used to authenticate at an authentication provider and then use returned tokens (such
  as OAuth) to authorize the actual request. In addition to that, it is able to renew the token, if it expired and
  re-sent the request. If exactly one `CallCredentials` bean is present on your application context then spring will
  automatically attach it to all `Stub`s (**NOT** `Channel`s). The
  [`CallCredentialsHelper`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/security/CallCredentialsHelper.html)
  utility class helps you to create commonly used `CallCredentials` types and related `StubTransformer`.
- [`StubFactory`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/stubfactory/StubFactory.html):
  A factory that can be used to create a specfic `Stub` type from a `Channel`. Multiple `StubFactory`s can be registered to support different stub types.
  See also [Configuration -> StubFactory](configuration.md#stubfactory).
- [`StubTransformer`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/inject/StubTransformer.html):
  A transformer that will be applied to all client `Stub`s before they are injected.
  See also [Configuration -> StubTransformer](configuration.md#stubtransformer).

### Accessing the Client

We recommended to inject (`@GrpcClient`) `Stub`s instead of plain `Channel`s.

> **Note:** There are different types of `Stub`s. Not all of them support all request types (streaming calls).

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
                .build()
        return myServiceStub.sayHello(request).getMessage();
    }

}
````

## Additional Topics <!-- omit in toc -->

- *Getting Started*
- [Configuration](configuration.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

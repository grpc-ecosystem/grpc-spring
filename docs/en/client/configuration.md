# Configuration

[<- Back to Index](../index.md)

This section describes how you can configure your grpc-spring-boot-starter clients.

## Table of Contents <!-- omit in toc -->

- [Configuration via Properties](#configuration-via-properties)
  - [Choosing the Target](#choosing-the-target)
- [Configuration via Beans](#configuration-via-beans)
  - [GrpcClientBean](#grpcclientbean)
  - [GrpcChannelConfigurer](#grpcchannelconfigurer)
  - [ClientInterceptor](#clientinterceptor)
  - [StubFactory](#stubfactory)
  - [StubTransformer](#stubtransformer)
- [Custom NameResolverProvider](#custom-nameresolverprovider)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- *Configuration*
- [Security](security.md)
- [Tests with Grpc-Stubs](testing.md)

## Configuration via Properties

grpc-spring-boot-starter can be
[configured](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) via
spring's `@ConfigurationProperties` mechanism.

You can find all build-in configuration properties here:

- [`GrpcChannelsProperties`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/config/GrpcChannelsProperties.html)
- [`GrpcChannelProperties`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/config/GrpcChannelProperties.html)
- [`GrpcServerProperties.Security`](https://static.javadoc.io/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/config/GrpcChannelProperties.Security.html)

If you prefer to read the sources instead, you can do so
[here](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/client/config/GrpcChannelProperties.java#L58).

The properties for the channels are all prefixed with `grpc.client.__name__.` and `grpc.client.__name__.security.`
respectively. The channel name is taken from the `@GrpcClient("__name__")` annotation.
If you wish to configure some options such as trusted certificates for all servers at once,
you can do so using `GLOBAL` as name.
Properties that are defined for a specific/named channel take precedence over `GLOBAL` ones.

### Choosing the Target

You can change the target server using the following property:

````properties
grpc.client.__name__.address=static://localhost:9090
````

There are a number of supported schemes, that you can use to determine the target server (Priorities 0 (low) - 10
(high)):

- `static` (Prio 4): \
  A simple static list of IPs (both v4 and v6), that can be use connect to the server (Supports `localhost`). \
  For resolvable hostnames please use `dns` instead. \
  Example: `static://192.168.1.1:8080,10.0.0.1:1337`
- [`dns`](https://github.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/internal/DnsNameResolver.java#L66)
  (Prio 5): \
  Resolves all addresses that are bound to the given DNS name. The addresses will be cached and will only be refreshed
  if an existing connection is shutdown/fails. \
  Example: `dns:///example.my.company` \
  Notice: There is also a `dns` resolver that is included in grpclb, that has a higher priority (`6`) than the default
  one and also supports `SVC` lookups. See also [Kubernetes Setup](../kubernetes.md).
- `discovery` (Prio 6): \
  (Optional) Uses spring-cloud's `DiscoveryClient` to lookup appropriate targets. The connections will be refreshed
  automatically during `HeartbeatEvent`s. Uses the `gRPC_port` metadata to determine the port, otherwise uses the
  service port. \
  Example: `discovery:///service-name`
- `self` (Prio 0): \
  The self address or scheme is a keyword that is available, if you also use `grpc-server-spring-boot-starter` and
  allows you to connect to the server without specifying the own address/port. This is especially useful for tests
  where you might want to use random server ports to avoid conflicts. \
  Example: `self:self`
- `in-process`: \
  This is a special scheme that will bypass the normal channel factory and will use the `InProcessChannelFactory`
  instead. Use it to connect to the [`InProcessServer`](../server/configuration.md#enabling-the-inprocessserver). \
  Example: `in-process:foobar`
- `unix` (Available on Unix based systems only): \
  This is a special scheme that uses unix's domain socket addresses to connect to a server. \
  If you are using `grpc-netty` you also need the `netty-transport-native-epoll` dependency.
  `grpc-netty-shaded` already contains that dependency, so there is no need to add anything for it to work. \
  Example: `unix:/run/grpc-server`
- `null`: \
  This is a special scheme that will bypass the normal channel factory and will use the `NullChannelFactory`
  instead. Use it to use `null` for a GrpcClient annotated field. \
  Useful for testing. \
  Example: `null:/`
- *custom*: \
  You can define custom
  [`NameResolverProvider`s](https://javadoc.io/page/io.grpc/grpc-all/latest/io/grpc/NameResolverProvider.html) those
  will be picked up, by either via Java's `ServiceLoader` or from spring's application context and registered in
  the `NameResolverRegistry`. \
  See also [Custom NameResolverProvider](#custom-nameresolverprovider)

If you don't define an address it will be guessed:

- First it will try it with just it's name (`<name>`)
- If you have configured a default scheme it will try that next (`<scheme>:/<name>`)
- Then it will use the default scheme of the `NameResolver.Factory` delegate (See the priorities above)

> **Note:** The number of slashes is important! Also make sure that you don't try to connect to a normal
> web/REST/non-grpc server (port).

The `SSL`/`TLS` and other security relevant configuration is explained on the [Client Security](security.md) page.

## Configuration via Beans

While this library intents to provide most of the features as configuration option, sometimes the overhead for adding it
is too high and thus we didn't add it, yet. If you feel like it is an important feature, feel free to open a feature
request.

If you want to change the application beyond what you can do through the properties, then you can use the existing
extension points that exist in this library.

First of all most of the beans can be replaced by custom ones, that you can configure in every way you want.
If you don't wish to go that far, you can use classes such as `GrpcChannelConfigurer` and `StubTransformer` to configure
the channels, stubs and other components without losing the features provided by this library.

### GrpcClientBean

This annotation is used to create injectable beans from your otherwise non-injectable `@GrpcClient` instances.
The annotation can be repeatedly added to any of your `@Configuration` classes.

> **Note:** We recommend using either `@GrpcClientBean`s or fields annotated with `@GrpcClient` throughout your
> application, as mixing the two might cause confusion for future developers.

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

The grpc client configurer allows you to add your custom configuration to grpc's `ManagedChannelBuilder`s.

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

> Be aware that depending on your configuration there might be different types of `ManagedChannelBuilder`s in the
> application context (e.g. the `InProcessChannelFactory`).

### ClientInterceptor

`ClientInterceptor`s can be used for various tasks, including:

- Authentication/Authorization
- Request validation
- Response filtering
- Attaching additional context to the call (e.g. tracing ids)
- Exception to error `Status` response mapping
- Logging
- ...

There are three ways to add a `ClientInterceptor` to your channel.

- Define the `ClientInterceptor` as a global interceptor using either the `@GrpcGlobalClientInterceptor` annotation,
  or a `GlobalClientInterceptorConfigurer`
- Explicitly list them in the `@GrpcClient#interceptors` or `@GrpcClient#interceptorNames` field
- Use a `StubTransformer` and call `stub.withInterceptors(ClientInterceptor... interceptors)`

The following examples demonstrate how to use annotations to create a global client interceptor:

````java
@Configuration
public class GlobalInterceptorConfiguration {

    @GrpcGlobalClientInterceptor
    LogGrpcInterceptor logClientInterceptor() {
        return new LogGrpcInterceptor();
    }

}
````

The following example demonstrates creation via `GlobalClientInterceptorConfigurer`

````java
@Configuration
public class GlobalInterceptorConfiguration {

    @Bean
    GlobalClientInterceptorConfigurer globalClientInterceptorConfigurer() {
        interceptors -> interceptors.add(new LogGrpcInterceptor());
    }

}
````

These variant are very handy if you wish to add third-party interceptors to the global scope.

For your own interceptor implementations you can achieve the same result by adding the annotation to the class itself:

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

A `StubFactory` is used to create a `Stub` of a specific type. The registered stub factories will be checked in order
and the first applicable factory will be used to create the stub.

This library has build in support for the `Stub` types defined in grpc-java:

- [`AsyncStubs`](https://grpc.github.io/grpc-java/javadoc/io/grpc/stub/AbstractAsyncStub.html)
- [`BlockingStubs`](https://grpc.github.io/grpc-java/javadoc/io/grpc/stub/AbstractBlockingStub.html)
- [`FutureStubs`](https://grpc.github.io/grpc-java/javadoc/io/grpc/stub/AbstractFutureStub.html)

But you can easily add support for other `Stub` types by adding a custom `StubFactory` to your application context.

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

> **Note:** Please report missing stub types (and the corresponding library) in our issue tracker so that we can add
> support if possible.

### StubTransformer

The stub transformer allows you to modify `Stub`s right before they are injected to your beans.
This is the recommended way to add `CallCredentials` to your stubs.

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

You can also use `StubTransformer`s to add custom `ClientInterceptor`s to your stub.

> **Note**: The `StubTransformer`s are applied after the  `@GrpcClient#interceptors(Names)` have been added.

## Custom NameResolverProvider

Sometimes you might have some custom logic that decides which server you wish to connect to, you can achieve this
using a custom `NameResolverProvider`.

> **Note:** This can only be used to decide this on an application level and not on a per request level.

This library provides some `NameResolverProvider`s itself so you can use them as a
[reference](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/client/nameresolver).

You can register your `NameResolverProvider` by adding it to `META-INF/services/io.grpc.NameResolverProvider` for Java's
`ServiceLoader` or adding it your spring context. If you wish to use some spring beans inside your `NameResolver`, then
you have to define it via spring context (unless you wish to use `static`).

> **Note:** `NameResolverProvider`s are registered gloablly, so might run into issues if you boot up two or more
> applications simulataneosly in the same JVM (e.g. during tests).

## HealthIndicator

This library automatically provides a `grpcChannel` `HealthIndicator` (actuator/health) for all clients (shared).
It will report the service as `OUT_OF_SERVICE` if any client has the `TRANSIENT_FAILURE` status.
If you wish to exclude it and provide more specific `HealthIndicator`s,
you can disable it by excluding the `GrpcClientHealthAutoConfiguration`

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- *Configuration*
- [Security](security.md)
- [Tests with Grpc-Stubs](testing.md)

----------

[<- Back to Index](../index.md)

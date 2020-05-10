# Configuration

[<- Back to Index](../index.md)

This section describes how you can configure your grpc-spring-boot-starter application.

## Table of Contents <!-- omit in toc -->

- [Configuration via Properties](#configuration-via-properties)
  - [Changing the Server Port](#changing-the-server-port)
  - [Enabling the InProcessServer](#enabling-the-inprocessserver)
- [Configuration via Beans](#configuration-via-beans)
  - [GrpcServerConfigurer](#grpcserverconfigurer)

## Additional topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- *Configuration*
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Security](security.md)

## Configuration via Properties

grpc-spring-boot-starter can be
[configured](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) via
spring's `@ConfigurationProperties` mechanism.

You can find all build-in configuration properties here:

- [`GrpcServerProperties`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/config/GrpcServerProperties.html)
- [`GrpcServerProperties.Security`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/config/GrpcServerProperties.Security.html)

If you prefer to read the sources instead, you can do so
[here](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/server/config/GrpcServerProperties.java#L50).

The properties for the server are all prefixed with `grpc.server.` and `grpc.server.security.` respectively.

### Changing the Server Port

If you wish to change the grpc server port from the default (`9090`) to a different port you can do so
using:

````properties
grpc.server.port=80
````

Set the port to `0` to use a free random port. This feature is intended for deployments with a discovery service and
concurrent tests.

> Please make sure that you won't run into conflicts with other applications or other endpoints such as `spring-web`.

The `SSL`/`TLS` and other security relevant configuration is explained on the [Server Security](security.md) page.

### Enabling the InProcessServer

Sometimes, you might want to consume your own grpc-service in your own application. You can do so like any other grpc server, but you can save the network overhead by using grpc's `InProcessServer`.

You can turn it on using the following property:

````properties
grpc.server.in-process-name=<SomeName>
# Optional: Turn off the external grpc-server
#grpc.server.port=-1
````

This allows clients to connect to the server from within the same application using the following configuration:

````properties
grpc.client.inProcess.address=in-process:<SomeName>
````

This is especially useful for tests as they don't need to open a specific port and thus can run concurrently (on a build
server).

## Configuration via Beans

While this library intents to provide most of the features as configuration option, sometimes the overhead for adding it
is too high and thus we didn't add it, yet. If you feel like it is an important feature, feel free to open a feature
request.

If you want to change the application beyond what you can do through the properties, then you can use the existing
extension points that exist in this library.

First of all most of the beans can be replaced by custom ones, that you can configure in every way you want.
If you don't wish to go that far, you can use classes such as `GrpcServerConfigurer` to configure the server and other
components without losing the features provided by this library.

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

> Be aware that depending on your configuration there might be different types of `ServerBuilder`s in the application
> context (e.g. the `InProcessServerBuilder`).

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- *Configuration*
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

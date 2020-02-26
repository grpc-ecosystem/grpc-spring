# gRPC Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)
[![MIT License](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)
[![Crowdin](https://badges.crowdin.net/grpc-spring-boot-starter/localized.svg)](https://crowdin.com/project/grpc-spring-boot-starter)

[![Client-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-client-spring-boot-autoconfigure.svg?label=Client-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-client-spring-boot-autoconfigure)
[![Server-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-server-spring-boot-autoconfigure.svg?label=Server-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-server-spring-boot-autoconfigure)
[![Common-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-common-spring-boot.svg?label=Common-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-common-spring-boot)

README: [English](README.md) | [中文](README-zh-CN.md)

**Documentation:** [English](https://yidongnan.github.io/grpc-spring-boot-starter/en/) | [中文](https://yidongnan.github.io/grpc-spring-boot-starter/zh-CN/)

## Features

* Auto configures and runs the embedded gRPC server with `@GrpcService`-enabled beans as part of your spring-boot
application

* Automatically creates and manages your grpc channels and stubs with `@GrpcClient`

* Supports [Spring Cloud](https://spring.io/projects/spring-cloud) (register services to [Consul](https://github.com/spring-cloud/spring-cloud-consul) or [Eureka](https://github.com/spring-cloud/spring-cloud-netflix) or [Nacos](https://github.com/spring-cloud-incubator/spring-cloud-alibaba) and fetch gRPC server information)

* Supports [Spring Sleuth](https://github.com/spring-cloud/spring-cloud-sleuth) as distributed tracing solution (If [brave-instrumentation-grpc](https://mvnrepository.com/artifact/io.zipkin.brave/brave-instrumentation-grpc) is present)

* Supports global and custom gRPC server/client interceptors

* [Spring-Security](https://github.com/spring-projects/spring-security) support

* Automatic metric support ([micrometer](https://micrometer.io/)/[actuator](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator) based)

* Also works with (non-shaded) grpc-netty

## Versions

2.x.x.RELEASE supports Spring Boot 2.1.x/2.2.x & Spring Cloud Greenwich/Hoxton.

The latest version: ``2.7.0.RELEASE``

(Use `2.4.0.RELEASE` for Spring Boot 2.0.x & Spring Cloud Finchley).

1.x.x.RELEASE support Spring Boot 1 & Spring Cloud Edgware, Dalston, Camden.

The latest version: ``1.4.2.RELEASE``

**Note:** This project can also be used without Spring-Boot, however that requires some manual bean configuration.

## Usage

### gRPC Server + Client

To add a dependency using Maven, use the following:

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-spring-boot-starter</artifactId>
  <version>2.7.0.RELEASE</version>
</dependency>
````

To add a dependency using Gradle:

````gradle
dependencies {
  compile 'net.devh:grpc-spring-boot-starter:2.7.0.RELEASE'
}
````

### gRPC Server

To add a dependency using Maven, use the following:

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>2.7.0.RELEASE</version>
</dependency>
````

To add a dependency using Gradle:

````gradle
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:2.7.0.RELEASE'
}
````

Annotate your server interface implementation(s) with ``@GrpcService``

````java
@GrpcService
public class GrpcServerService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello ==> " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
````

By default, the grpc server will listen to port `9090`. These and other
[settings](grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/server/config/GrpcServerProperties.java)
can be changed via Spring's property mechanism. The server uses the `grpc.server.` prefix.

Refer to our [documentation](https://yidongnan.github.io/grpc-spring-boot-starter/) for more details.

### gRPC Client

To add a dependency using Maven, use the following:

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>2.7.0.RELEASE</version>
</dependency>
````

To add a dependency using Gradle:

````gradle
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:2.7.0.RELEASE'
}
````
Annotate a field of your grpc client stub with `@GrpcClient(serverName)`

* Do not use in conjunction with `@Autowired` or `@Inject`

  ````java
  @GrpcClient("gRPC server name")
  private GreeterGrpc.GreeterBlockingStub greeterStub;
  ````

**Note:** You can use the same grpc server name for multiple channels and also different stubs (even with different
interceptors).

Then you can send queries to your server just like this:

````java
HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName(name).build());
````

It is possible to configure the target address for each client individually.
However in some cases, you can just rely on the default configuration.
You can customize the default url mapping via `NameResolver.Factory` beans. If you don't configure that bean,
then the default uri will be guessed using the default scheme and the name (e.g.: `dns:/<name>`):

These and other
[settings](grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/client/config/GrpcChannelProperties.java)
can be changed via Spring's property mechanism. The clients use the `grpc.client.(serverName).` prefix.

Refer to our [documentation](https://yidongnan.github.io/grpc-spring-boot-starter/) for more details.

## Running with (non-shaded) grpc-netty

This library supports both `grpc-netty` and `grpc-netty-shaded`.
The later one might prevent conflicts with incompatible grpc-versions or conflicts between libraries that require different versions of netty.

**Note:** If the shaded netty is present on the classpath, then this library will always favor it over the non-shaded grpc-netty one.

You can use it with Maven like this:

````xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty</artifactId>
    <version>${grpcVersion}</version>
</dependency>

<!-- For both -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- For the server (only) -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-server-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- For the client (only) -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
        </exclusion>
    </exclusions>
</dependency>
````

and like this when using Gradle:

````groovy
compile "io.grpc:grpc-netty:${grpcVersion}"

compile 'net.devh:grpc-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For both
compile 'net.devh:grpc-client-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For the client (only)
compile 'net.devh:grpc-server-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For the server (only)
````

## Example-Projects

Read more about our example projects [here](examples).

## Troubleshooting

Refer to our [documentation](https://yidongnan.github.io/grpc-spring-boot-starter/en/trouble-shooting) for help.

## Contributing

Contributions are always welcomed! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

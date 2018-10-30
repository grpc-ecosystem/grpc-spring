# Grpc Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)
[![MIT License](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)

README: [English](README.md) | [中文](README-zh.md)

## Features

* Auto configures and runs the embedded gRPC server with `@GrpcService`-enabled beans as part of your spring-boot
application

* Automatically creates and manages your grpc channels and stubs with `@GrpcClient`

* Supports Spring Cloud (register services to Consul or Eureka and fetch gRPC server information)

* Supports Spring Sleuth to trace application

* Supports global and custom gRPC server/client interceptors

## Versions

2.x.x.RELEASE support Spring Boot 2 & Spring Cloud Finchley.
 
The latest version: ``2.0.1.RELEASE``

1.x.x.RELEASE support Spring Boot 1 & Spring Cloud Edgware, Dalston, Camden.

The latest version: ``1.4.1.RELEASE``

**Note:** This project can also be used without Spring-Boot, however that requires some manual bean configuration.

## Usage

### gRPC server

To add a dependency using Maven, use the following:

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>2.1.0.RELEASE</version>
</dependency>
````

To add a dependency using Gradle:

````gradle
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:2.1.0.RELEASE'
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

By default, the grpc server will listen to port 9090. These and other
[settings](grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/springboot/autoconfigure/grpc/server/GrpcServerProperties.java)
can be changed via Spring's property mechanism. The server uses the `grpc.server.` prefix.

#### Example-Properties

````properties
grpc.server.port=9090
grpc.server.address=0.0.0.0
````

### gRPC client

To add a dependency using Maven, use the following:

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>2.1.0.RELEASE</version>
</dependency>
````

To add a dependency using Gradle:

````gradle
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:2.1.0.RELEASE'
}
````

There are three ways to get a connection to the gRPC server:

* Use `grpcChannelFactory.createChannel(serverName)` to create a `Channel` and create the grpc stub
  yourself.

  ````java
  @Autowired
  private GrpcChannelFactory grpcChannelFactory;

  private GreeterGrpc.GreeterBlockingStub greeterStub;

  @PostConstruct
  public void init() {
      Channel channel = grpcChannelFactory.createChannel("gRPC server name");
      greeterStub = GreeterGrpc.newBlockingStub(channel);
  }
  ````

* Annotate a field of type `Channel` with `@GrpcClient(serverName)` and create the grpc stub yourself.
  * Do not use in conjunction with `@Autowired` or `@Inject`
  
  ````java
  @GrpcClient("gRPC server name")
  private Channel channel;

  private GreeterGrpc.GreeterBlockingStub greeterStub;

  @PostConstruct
  public void init() {
      greeterStub = GreeterGrpc.newBlockingStub(channel);
  }
  ````
  
* Annotate a field of your grpc client stub with `@GrpcClient(serverName)`
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

By default, the client assumes that the server runs on `127.0.0.1` with port `9090`. These and other
[settings](grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/springboot/autoconfigure/grpc/client/GrpcChannelProperties.java)
can be changed via Spring's property mechanism. The clients use the `grpc.client.(serverName).` prefix.

#### Example-Properties

````properties
grpc.client.(gRPC server name).host[0]=127.0.0.1
grpc.client.(gRPC server name).port[0]=9090
# Or
grpc.client.myName.host=127.0.0.1
grpc.client.myName.port=9090
````

## Troubleshooting

### Issues with SSL in general

* `java.lang.IllegalStateException: Failed to load ApplicationContext`
* `Caused by: org.springframework.context.ApplicationContextException: Failed to start bean 'grpcServerLifecycle'`
* `Caused by: java.lang.IllegalStateException: Could not find TLS ALPN provider; no working netty-tcnative, Conscrypt, or Jetty NPN/ALPN available`

or

* `AbstractMethodError: io.netty.internal.tcnative.SSL.readFromSSL()`

You don't have the (correct) library or version of `netty-tcnative...` in your classpath.

(There is a breaking change between netty 4.1.24.Final -> 4.1.27.Final and netty-tcnative 2.0.8.Final -> 2.0.12.Final)

See also [grpc-java: Security](https://github.com/grpc/grpc-java/blob/master/SECURITY.md).

### Issues with SSL during tests

By default, the grpc client assumes that the server uses TLS and will try to use a secure connection. During development
and for tests is it unlikely that the required certificates are available thus you have to switch to `PLAINTEXT`
connection mode.

````properties
grpc.client.(gRPC server name).negotiationType=PLAINTEXT
````

**Note:** The grpc protocol and we strongly recommend using `TLS` for production use.

### Server port already in use during tests

Sometimes you just want to launch your application in your test to test the interaction between your services.
This will also start the grpc server, however it won't be shut down after each test (class). You can avoid that issue by
adding `@DirtiesContext` to your test classes or methods.

### No name matching XXX found

* `io.grpc.StatusRuntimeException: UNAVAILABLE: io exception`
* `Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem`
* `Caused by: java.security.cert.CertificateException: No name matching gRPC server name found`

The name of the `gRPC server name` in the client config does not match the common / alternative name in the server
certificate. You have to configure the `grpc.client.(gRPC server name).security.authorityOverride`
property with a name that is present in the certificate.

## Contributing

Contributions are always welcomed! Please see [CONTRIBUTING.md] for detailed guidelines.

## Show case

https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples

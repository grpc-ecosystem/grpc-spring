# gRPC Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)
[![MIT License](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)

[![Client-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-client-spring-boot-autoconfigure.svg?label=Client-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-client-spring-boot-autoconfigure)
[![Server-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-server-spring-boot-autoconfigure.svg?label=Server-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-server-spring-boot-autoconfigure)
[![Common-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-common-spring-boot.svg?label=Common-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-common-spring-boot)

README: [English](README.md) | [中文](README-zh.md)

**GitHub地址：https://github.com/yidongnan/grpc-spring-boot-starter**

Java技术交流群：294712648 <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=34ad403ce78380042406f11a122637ea9d66c11ae20f331dff37bc90a4fde939"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Java技术交流群" title="Java技术交流群"></a>

## 特点

* 使用`@ GrpcService`自动创建并运行一个 gRPC 服务，内嵌在 spring-boot 应用中

* 使用`@ GrpcClient`自动创建和管理你的``channel``和``stub``

* 支持 [Spring Cloud](https://spring.io/projects/spring-cloud)（向 [Consul](https://github.com/spring-cloud/spring-cloud-consul) 或 [Eureka](https://github.com/spring-cloud/spring-cloud-netflix) 或 [Nacos](https://github.com/spring-cloud-incubator/spring-cloud-alibaba) 注册服务并获取gRPC服务信息）

* 支持 [Spring Sleuth](https://github.com/spring-cloud/spring-cloud-sleuth) 进行链路跟踪(需要单独引入 [brave-instrumentation-grpc](https://mvnrepository.com/artifact/io.zipkin.brave/brave-instrumentation-grpc))

* 支持对 server、client 分别设置全局拦截器或单个的拦截器

* 支持 [Spring-Security](https://github.com/spring-projects/spring-security)

* 支持 metric ([micrometer](https://micrometer.io/) / [actuator](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator))

* 可以使用 (non-shaded) grpc-netty

## 版本

2.x.x.RELEASE 支持 Spring Boot 2 & Spring Cloud Finchley, Greenwich。

最新的版本：``2.4.0.RELEASE``

1.x.x.RELEASE 支持 Spring Boot 1 & Spring Cloud Edgware 、Dalston、Camden。

最新的版本：``1.4.2.RELEASE``

**注意:** 此项目也可以在没有 Spring-Boot 的场景下使用，但需要手动的配置相关的 bean。

## 使用方式

### gRPC server + client

如果使用的是 Maven，添加如下依赖

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-spring-boot-starter</artifactId>
  <version>2.4.0.RELEASE</version>
</dependency>
````

如果使用的 Gradle，添加如下依赖

````gradle
dependencies {
  compile 'net.devh:grpc-spring-boot-starter:2.4.0.RELEASE'
}
````

### gRPC 服务端

如果使用的是 Maven，添加如下依赖

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>2.4.0.RELEASE</version>
</dependency>
````

如果使用的 Gradle，添加如下依赖

````gradle
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:2.4.0.RELEASE'
}
````

实现 gRPC server 的业务逻辑，并使用 ``@GrpcService`` 注解

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

设置 gRPC 的 host 跟 port ，默认的监听的 port 是 9090。其他配置属性可以参考
[settings](grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/server/config/GrpcServerProperties.java)。
所有的配置属性在 server 中使用需增加 `grpc.server.` 的前缀

#### 服务端配置属性示例

````properties
grpc.server.port=9090
grpc.server.address=0.0.0.0
#grpc.server.inProcessName=test
````

#### 对 Server 进行自定义

当前项目同样支持对 `ServerBuilder` 的自定义修改，需要在创建的过程中使用 `GrpcServerConfigurer` beans。

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

#### Server-Security
支持使用 Spring-Security 加密你的 gRPC 应用。你只需要添加 Spring-Security（core 或者 config）依赖，然后根据需要再增加加密的配置

##### 首先需要选择一个认证方案


* **BasicAuth（基础认证）**

  ````java
  @Bean
  AuthenticationManager authenticationManager() {
      final List<AuthenticationProvider> providers = new ArrayList<>();
      providers.add(...); // Possibly DaoAuthenticationProvider
      return new ProviderManager(providers);
  }

  @Bean
  GrpcAuthenticationReader authenticationReader() {
      final List<GrpcAuthenticationReader> readers = new ArrayList<>();
      readers.add(new BasicGrpcAuthenticationReader());
      return new CompositeGrpcAuthenticationReader(readers);
  }
  ````

* **Bearer Authentication (OAuth2/OpenID-Connect)**

  ````java
  @Bean
  AuthenticationManager authenticationManager() {
      final List<AuthenticationProvider> providers = new ArrayList<>();
      providers.add(...); // Possibly JwtAuthenticationProvider
      return new ProviderManager(providers);
  }

  @Bean
  GrpcAuthenticationReader authenticationReader() {
      final List<GrpcAuthenticationReader> readers = new ArrayList<>();
      readers.add(new BearerAuthenticationReader(accessToken -> new BearerTokenAuthenticationToken(accessToken)));
      return new CompositeGrpcAuthenticationReader(readers);
  }
  ````

  你可能还想定义自己的 *GrantedAuthoritiesConverter* ，将权限和角色的信息映射到 Spring Security 的 `GrantedAuthority` 中

* **Certificate Authentication（证书认证）**

  ````java
  @Bean
  AuthenticationManager authenticationManager() {
      final List<AuthenticationProvider> providers = new ArrayList<>();
      providers.add(new X509CertificateAuthenticationProvider(userDetailsService()));
      return new ProviderManager(providers);
  }

  @Bean
  GrpcAuthenticationReader authenticationReader() {
      final List<GrpcAuthenticationReader> readers = new ArrayList<>();
      readers.add(new SSLContextGrpcAuthenticationReader());
      return new CompositeGrpcAuthenticationReader(readers);
  }
  ````

  相关的配置属性如下：

  ````properties
  grpc.server.security.enabled=true
  grpc.server.security.certificateChainPath=certificates/server.crt
  grpc.server.security.privateKeyPath=certificates/server.key
  grpc.server.security.trustCertCollectionPath=certificates/trusted-clients-collection
  grpc.server.security.clientAuth=REQUIRE
  ````

* **使用 `CompositeGrpcAuthenticationReader` 类链式的调用多个认证方案**
* **自定义认证方式(继承并实现 `GrpcAuthenticationReader` 类)**

##### 如何去保护你的这些服务

* **使用 Spring-Security 的注解**

  ````java
  @Configuration
  @EnableGlobalMethodSecurity(proxyTargetClass = true, ...)
  public class SecurityConfiguration {
  ````

  如果你想使用 Spring Security 相关的注解的话，`proxyTargetClass` 属性是必须的！
  但是你会受到一条警告，提示 MyServiceImpl#bindService() 方式是用 final 进行修饰的。
  这条警告目前无法避免，但它是安全的，可以忽略它。

* **手动配置**

  ````java
  @Bean
  AccessDecisionManager accessDecisionManager() {
      final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
      voters.add(new AccessPredicateVoter());
      return new UnanimousBased(voters);
  }

  @Bean
  GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
      final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();
      source.set(MyServiceGrpc.getSecureMethod(), AccessPredicate.hasRole("ROLE_USER"));
      source.setDefault(AccessPredicate.permitAll());
      return source;
  }
  ````

### gRPC 客户端

如果使用的是 Maven，添加如下依赖

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-client-spring-boot-starter</artifactId>
  <version>2.4.0.RELEASE</version>
</dependency>
````

如果使用的 Gradle，添加如下依赖

````gradle
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:2.4.0.RELEASE'
}
````

这里有三种方式去或得一个gRPC server的连接


* 使用 `grpcChannelFactory.createChannel(serverName)` 去创建一个 `Channel`，并创建一个自己的 gRPC stub.

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

* 通过在 `Channel` 类型的字段上加入 `@GrpcClient(serverName)` 注解，并创建一个自己的 gRPC stub.
  * 不需要使用 `@Autowired` 或者 `@Inject` 来进行注入

  ````java
  @GrpcClient("gRPC server name")
  private Channel channel;

  private GreeterGrpc.GreeterBlockingStub greeterStub;

  @PostConstruct
  public void init() {
      greeterStub = GreeterGrpc.newBlockingStub(channel);
  }
  ````

* 直接将 `@GrpcClient(serverName)` 注解加在调用客户端的 stub 上
  * 不需要使用 `@Autowired` 或者 `@Inject` 来进行注入

  ````java
  @GrpcClient("gRPC server name")
  private GreeterGrpc.GreeterBlockingStub greeterStub;
  ````

**注意:** 你可以为多个 channels 和多个不同的 stubs 使用相同的 serverName (除非他们拦截器不一样).

然后你可以直接向服务端发起请求，如下:

````java
HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName(name).build());
````

可以单独为每一个 client 配置对应的 address
但在某些情况下，你可以调整默认的配置。
你可以通过 `NameResolver.Factory` beans 去自定义默认的 url 映射，如果你没有配置这个 bean，那将会按照下面的方式进行解析：

* 如果存在一个 `DiscoveryClient` 的 bean，这时会使用 client name 去注册中心上进行获取对应服务的 address
* 否则 client 端将使用 `localhost` 和 `9090` 端口

其他的配置属性参考 [settings](grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/boot/grpc/client/config/GrpcChannelProperties.java)，所有的配置文件在 client 端使用时需要增加 `grpc.client.(serverName).`的前缀

你也可以配置多个目标地址，请求时会自动使用负载均衡

* `static://127.0.0.1:9090,[::1]:9090`

你也可以使用服务发现去获取目标地址（要求一个 `DiscoveryClient` bean）

* `discovery:///my-service-name`

此外，你也可以使用 DNS 的方式去获取目标地址

* `dns:///example.com`

同时，你也可以使用如下方式直接进程内访问

* `in-process:test`

它会通过DNS将域名解析出所有真实的 IP 地址，通过使用这些真实的IP地址去做负载均衡。
需要注意的是 `grpc-java` 出于性能的考虑对 DNS 返回的结果做缓存。
有关这些和其他原生支持的 `NameResolverProviders` 参考官方文档 [grpc-java sources](https://github.com/grpc/grpc-java)

#### 客户端配置属性示例

````properties
grpc.client.GLOBAL.enableKeepAlive=true

grpc.client.(gRPC server name).address=static://localhost:9090
# Or
grpc.client.myName.address=static://localhost:9090
````

`GLOBAL` 是一个特殊的常量，它可以用于对所有 Client 统一的设置属性。
属性覆盖的顺序：Client单独的属性 > `GLOBAL`的属性 > 默认的属性

#### 自定义 Client

This library also supports custom changes to the `ManagedChannelBuilder` and gRPC client stubs during creation by creating `GrpcChannelConfigurer` and `StubTransformer` beans.
当前项目支持对 `ManagedChannelBuilder` 的自定义，在 gRPC client stub创建的过程中，通过使用 `GrpcChannelConfigurer` 或 `StubTransformer` bean
来完成自定义操作

````java
@Bean
public GrpcChannelConfigurer keepAliveClientConfigurer() {
  return (channelBuilder, name) -> {
    if (channelBuilder instanceof NettyChannelBuilder) {
      ((NettyChannelBuilder) channelBuilder)
          .keepAliveTime(15, TimeUnit.SECONDS)
          .keepAliveTimeout(5, TimeUnit.SECONDS);
    }
  };
}

@Bean
public StubTransformer authenticationStubTransformer() {
  return (clientName, stub) -> stub.withCallCredentials(grpcCredentials(clientName));
}
````

#### 客户端认证

**注意:** 以下列出的一些方法仅仅适用于通过注入得到的 stubs，如果你通过注入 Channel，手动的在去创建 stubs，这就需要你自己手动的
去配置凭证。然而你同样能从目前所提供的一些辅助类方法中收益。

客户端有许多不同的认证方式，我们只需定义一个类型为 `CallCredentials` 的 bean，它会自动作用于身份验证。目前通过一些辅助方法可以支持
下列的认证方式：

* **BasicAuth**

  ````java
  @Bean
  CallCredentials grpcCredentials() {
    return CallCredentialsHelper.basicAuth(username, password);
  }
  ````

* **Bearer Authentication (OAuth2, OpenID-Connect)**

  ````java
  @Bean
  CallCredentials grpcCredentials() {
    return CallCredentialsHelper.bearerAuth(token);
  }
  ````

* **Certificate Authentication**

  需要一些配置属性：

  ````properties
  #grpc.client.test.security.authorityOverride=localhost
  #grpc.client.test.security.trustCertCollectionPath=certificates/trusted-servers-collection
  grpc.client.test.security.clientAuthEnabled=true
  grpc.client.test.security.certificateChainPath=certificates/client.crt
  grpc.client.test.security.privateKeyPath=certificates/client.key

* **为每个 client 使用不同的认证**

  通过定义一个 `StubTransformer` bean 来代替原有的 `CallCredentials` bean

  ````java
  @Bean
  StubTransformer grpcCredentialsStubTransformer() {
    return CallCredentialsHelper.mappedCredentialsStubTransformer(
        Map.of(
            clientA, callCredentialsAC,
            clientB, callCredentialsB,
            clientC, callCredentialsAC));
  }
  ````

  **注意:** 如果你配置了 `CallCredentials` bean，然后再使用 `StubTransformer` 的话可能会造成冲突。

## 使用 (non-shaded)grpc-netty

当前项目目前支持 `grpc-netty` 和 `grpc-netty-shaded`。
使用 `grpc-netty-shaded` 可以防止 grpc 跟 netty 版本的兼容性问题。

**注意:** 如果 `grpc-netty-shaded` 已经存在于 classpath 中, 那么将优先使用 shaded-netty

如果你使用的Maven，你可以使用如下的配置：

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

如果你使用的 Gradle，你可以使用如下的配置：

````groovy
compile "io.grpc:grpc-netty:${grpcVersion}"

compile 'net.devh:grpc-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For both
compile 'net.devh:grpc-client-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For the client (only)
compile 'net.devh:grpc-server-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty-shaded' // For the server (only)
````

## 示例项目

查看更多的示例项目 [here](examples).

## 常见的问题

在你深入去查问题之前，请先确保 grpc 跟 netty 的版本是彼此兼容的。
当前项目自带的依赖会确保 grpc 和 netty 是能一起正常工作。
但是在某些情况下，你可能需要用到其他库（如 tcnative ）或其他依赖项需要用到不同的 netty 版本，这就可能会造成版本冲突。
为了防止此类问题，gRPC 和 我们建议你使用 grpc-netty-shaded 依赖。
如果你使用 (non-shaded) grpc-netty，请查看[表格](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty)中展示的[grpc-java](https://github.com/grpc/grpc-java) 的兼容版本

### SSL 相关的问题

* `java.lang.IllegalStateException: Failed to load ApplicationContext`
* `Caused by: org.springframework.context.ApplicationContextException: Failed to start bean 'grpcServerLifecycle'`
* `Caused by: java.lang.IllegalStateException: Could not find TLS ALPN provider; no working netty-tcnative, Conscrypt, or Jetty NPN/ALPN available`

or

* `AbstractMethodError: io.netty.internal.tcnative.SSL.readFromSSL()`

or

* `javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem`

您的类路径中没有 `netty-tcnative ...`库或者没有正确的版本。

(在netty 4.1.24.Final -> 4.1.24.Final 和 netty-tcnative 2.0.8.Final -> 2.0.12.Final 版本之间存在非向下兼容更新)

查看 [grpc-java: Security](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty).

### 测试期间 SSL 的问题

默认情况下，gRPC 客户端假定服务器使用的 TLS，并尝试使用安全连接，在开发和测试期间，一般都不需要证书，你可以切换到 `PLAINTEXT` 的连接方式。

````properties
grpc.client.(gRPC server name).negotiationType=PLAINTEXT
````

**注意:** 在生产环境，我们强烈推荐你使用 `TLS` 的模式。

### 在测试模式下，端口已经被使用

有时候你只想启动你的应用程序去检测服务之间的交互，那么这将启动 gRPC 服务，然后你不需要为每个测试方法或测试类单独的去关闭 gRPC 服务，
在你的测试类或者方法中使用 `@DirtiesContext` 注解可以避免这个问题。

### 找不到与 xxx 匹配的名称

* `io.grpc.StatusRuntimeException: UNAVAILABLE: io exception`
* `Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem`
* `Caused by: java.security.cert.CertificateException: No name matching gRPC server name found`

客户端配置的 `gRPC server name` 的名称与服务器上公用或者备用的证书名称不匹配。你必须将 `grpc.client.(gRPC server name).security.authorityOverride` 属性设置成一个存在的名称。

## 贡献

我们欢迎任何人为这个项目做出自己的贡献! 贡献时需要参考 [CONTRIBUTING.md](CONTRIBUTING.md) 文档.

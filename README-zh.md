# gRPC Spring Boot Starter

[![Build Status](https://travis-ci.org/yidongnan/grpc-spring-boot-starter.svg?branch=master)](https://travis-ci.org/yidongnan/grpc-spring-boot-starter)
[![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/net.devh/grpc-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.devh%22%20grpc)
[![MIT License](https://img.shields.io/github/license/mashape/apistatus.svg)](LICENSE)

[![Client-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-client-spring-boot-autoconfigure.svg?label=Client-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-client-spring-boot-autoconfigure)
[![Server-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-server-spring-boot-autoconfigure.svg?label=Server-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-server-spring-boot-autoconfigure)
[![Common-Javadoc](https://www.javadoc.io/badge/net.devh/grpc-common-spring-boot-autoconfigure.svg?label=Common-Javadoc)](https://www.javadoc.io/doc/net.devh/grpc-common-spring-boot-autoconfigure)

README: [English](README.md) | [中文](README-zh.md)

Java技术交流群：294712648 <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=34ad403ce78380042406f11a122637ea9d66c11ae20f331dff37bc90a4fde939"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="Java技术交流群" title="Java技术交流群"></a>

## 特点

* 使用`@ GrpcService`自动创建并运行一个 gRPC 服务，内嵌在 spring-boot 应用中

* 使用`@ GrpcClient`自动创建和管理你的``channel``和``stub``

* 支持 [Spring Cloud](https://spring.io/projects/spring-cloud)（向Consul或Eureka注册服务并获取gRPC服务器信息）

* 支持 [Spring Sleuth](https://github.com/spring-cloud/spring-cloud-sleuth) 进行链路跟踪

* 支持对于 server、client 分别设置全局拦截器或单个的拦截器

* 支持 [Spring-Security](https://github.com/spring-projects/spring-security)

* 支持 metric ([micrometer](https://micrometer.io/)/[actuator](https://github .com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-actuator) based)

* 可以使用 grpc-netty-shaded

## 版本

2.x.x.RELEASE 支持 Spring Boot 2 & Spring Cloud Finchley。

最新的版本：``2.1.0.RELEASE``

1.x.x.RELEASE 支持 Spring Boot 1 & Spring Cloud Edgware 、Dalston、Camden。

最新的版本：``1.4.1.RELEASE``

**注意:** 此项目也可以在没有Spring-Boot的情况下使用，但这需要一些手动bean配置。

## 使用方式

### gRPC server + client

如果使用的是 Maven，添加如下依赖

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-spring-boot-starter</artifactId>
  <version>2.2.0.RELEASE</version>
</dependency>
````

如果使用的 Gradle，添加如下依赖

````gradle
dependencies {
  compile 'net.devh:grpc-spring-boot-starter:2.2.0.RELEASE'
}
````

### gRPC 服务端

如果使用的是 Maven，添加如下依赖

````xml
<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>2.2.0.RELEASE</version>
</dependency>
````

如果使用的 Gradle，添加如下依赖

````gradle
dependencies {
  compile 'net.devh:grpc-server-spring-boot-starter:2.2.0.RELEASE'
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

设置 gRPC 的 host 跟 port ，默认的监听的 host 是 0.0.0.0，默认的 port 是 9090。其他配置属性可以参考
[settings](grpc-server-spring-boot-autoconfigure/src/main/java/net/devh/springboot/autoconfigure/grpc/server
/GrpcServerProperties.java)。所有的配置文件在 server 中使用需增加 `grpc.server.` 的前缀

#### Properties示例

````properties
grpc.server.port=9090
grpc.server.address=0.0.0.0
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

##### 然后决定如果去保护你的服务

* **使用 Spring-Security 的注解**

  ````java
  @Configuration
  @EnableGlobalMethodSecurity(proxyTargetClass = true, ...)
  public class SecurityConfiguration {
  ````

  如果你想使用 Spring Security 相关的注解的话，`proxyTargetClass` 属性是必须的！
  但是你会受到一条警告，提示 MyServiceImpl#bindService() 方式是用 final 进行修饰的。
  这条警告目前无法避免，单他是安全的，可以忽略它。

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
  <version>2.2.0.RELEASE</version>
</dependency>
````

如果使用的 Gradle，添加如下依赖

````gradle
dependencies {
  compile 'net.devh:grpc-client-spring-boot-starter:2.2.0.RELEASE'
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

* 直接将 `@GrpcClient(serverName)` 注解加在你自己的 stub 上
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

其他的配置属性参考 [settings](grpc-client-spring-boot-autoconfigure/src/main/java/net/devh/springboot/autoconfigure/grpc/client
/GrpcChannelProperties.java)，所有的配置文件在 client 端使用时需要增加 `grpc.client.(serverName).`的前缀

你也可以配置多个目标地址，请求时会自动使用负载均衡

* `static://127.0.0.1:9090,[::1]:9090`

你也可以使用服务发现去获取目标地址（要求一个 `DiscoveryClient` bean）

* `discovery:///my-service-name`

此外，你也可以使用 DNS 的方式去获取目标地址

* `dns:///example.com`

#### Properties示例

````properties
grpc.client.(gRPC server name).address=static://localhost:9090
# Or
grpc.client.myName.address=static://localhost:9090
````

#### 客户端认证

客户端认证有很多种不同的方式，但目前仅仅支持其中的一部分，支持列表如下：

* **BasicAuth**

  使用 `ClientInterceptor` (其他认证机制可以以类似的方式实现).

  ````java
  @Bean
  ClientInterceptor basicAuthInterceptor() {
      return AuthenticatingClientInterceptors.basicAuth(username, password);
  }
  ````

  * 为所有的 client 设置相同的认证

    ````java
    @Bean
    public GlobalClientInterceptorConfigurer basicAuthInterceptorConfigurer() {
        return registry -> registry.addClientInterceptors(basicAuthInterceptor());
    }
    ````

  * 每个 client 使用不同的认证

    ````java
    @GrpcClient(value = "myClient", interceptorNames = "basicAuthInterceptor")
    private MyServiceStub myServiceStub;
    ````

* **Certificate Authentication**

  需要一些配置属性：

  ````properties
  #grpc.client.test.security.authorityOverride=localhost
  #grpc.client.test.security.trustCertCollectionPath=certificates/trusted-servers-collection
  grpc.client.test.security.clientAuthEnabled=true
  grpc.client.test.security.certificateChainPath=certificates/client.crt
  grpc.client.test.security.privateKeyPath=certificates/client.key
  ````

## 使用 grpc-netty-shaded

该库也支持 `grpc-netty-shaded` 库

**注意:** 如果 shaded netty 已经存在于 classpath 中, 那么将优先使用这个库

如果你使用的Maven，你可以使用如下的配置：

````xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
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
            <artifactId>grpc-netty</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- For the server -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-server-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- For the client -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
    <version>...</version>
    <exclusions>
        <exclusion>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty</artifactId>
        </exclusion>
    </exclusions>
</dependency>
````

如果你使用的 Gradle，你可以使用如下的配置：

````groovy
compile "io.grpc:grpc-netty-shaded:${grpcVersion}"

compile 'net.devh:grpc-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty' // For both
compile 'net.devh:grpc-client-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty' // For the client
compile 'net.devh:grpc-server-spring-boot-starter:...' exclude group: 'io.grpc', module: 'grpc-netty' // For the server
````

## 示例项目

查看更多的示例项目 [here](examples).

## 贡献

我们总是欢迎大家为这个项目做出自己的贡献! 贡献时需要参考 [CONTRIBUTING.md](CONTRIBUTING.md) 文档.

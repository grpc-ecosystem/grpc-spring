# 客户端安全

[<- 返回索引](../index.md)

此页面描述了您如何连接到 gRPC 服务器并进行身份验证。

## 目录 <!-- omit in toc -->

- [启用传输图层安全](#启用传输层安全)
  - [基础要求](#基础要求)
- [禁用传输图层安全](#禁用传输层安全)
  - [信任服务器](#信任服务端)
- [双向证书认证](#双向证书认证)
- [身份验证](#身份认证)
  - [创建 CallCredentials](#创建 CallCredentials)
  - [使用 CallCredentials](#使用 CallCredentials)
  - [重试时使用新的令牌做验证](#重试时使用新的令牌做认证)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- *安全性*
- [使用 Grpc-Stubs 测试](testing.md)

## 启用传输层安全

gRPC 默认使用 `TLS` 连接服务端，因此无需执行其他任何操作。

如果您想要检查您是否意外覆盖配置， 请检查给定的属性有这样配置：

````properties
grpc.client.<SomeName>.negotiationType=TLS
````

对于服务端的配置，请参考 [服务端安全](../server/security.md) 页面。

### 基础要求

如同往常一样，需要满足一些简单的前提条件：

- 在您的 classpath 上有兼容的 `SSL `/`TLS` 实现
  - 包含 [grpc-netty-shaded](https://mvnrepository.com/artifact/io.grpc/grpc-netty-shaded)
  - 对于[`grpc-netty`](https://mvnrepository.com/artifact/io.grpc/grpc-netty)，还需要额外添加 [`nety-tcnative-boringssl-static`](https://mvnrepository.com/artifact/io.netty/netty-tcnative-boringssl-static) 依赖。 (请使用 [grpc-java的 Netty 安全部分](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty) 表中列出**完全相同** (兼容)的版本)。

## 禁用传输层安全

> **警告:** 请勿在生产环境中这样做。

有时您没有可用的证书(例如在开发期间)，因此您可能希望禁用传输层安全，您可以这样做：

````properties
grpc.client.__name__.negotiationType=PLAINTEXT
````

下面的示例演示如何在测试中配置此属性：

````java
@SpringBootTest(properties = "grpc.client.test.negotiationType=PLAINTEXT")
@SpringJUnitConfig(classes = TestConfig.class)
@DirtiesContext
public class PlaintextSetupTest {

    @GrpcClient("test")
    private MyServiceBlockingStub myService;
````

### 信任服务端

如果您信任的证书不在常规信任存储区， 或者您想要限制您信任的 证书。您可以使用以下属性：

````properties
grpc.client.__name__.security.trustCertCollection=file:trusted-server.crt.collection
````

如果您想知道这里支持哪些选项，请阅读 [Spring 的 Resource 文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources-resourceloader)。

如果您使用服务标识符，证书可能会出现问题，因为它对内部服务名称无效。 在这种情况下，您可以需要指定证书对哪个名字有效：

````properties
grpc.client.__name__.security.authorityOverride=localhost
````

## 双向证书认证

在安全环境中，您可能必须使用客户端证书进行身份验证。 该证书通常由服务端提供，因此您需要为您的应用程序配置如下属性：

````properties
grpc.client.__name__.security.clientAuthEnabled=true
grpc.client.__name__.security.certificateChain=file:certificates/client.crt
grpc.client.__name__.security.privateKey=file:certificates/client.key
````

## 身份认证

### 创建 CallCredentials

除了双向证书认证外，还有其他几种认证方式，如 `BasicAuth`。

grpc-spring-boot-starter 除了一些帮助方法，同时提供了 BasicAuth 的实现。 然而，这里有很多库可以为 [`CallCredentials`](https://grpc.github.io/grpc-java/javadoc/io/grpc/CallCredentials.html)提供实现功能。 `CallCredentials` 是一个可扩展的组件，因为它们可以使用（第三方）服务队请求进行身份验证，并且可以自己管理和更新会话 token。

````java
@Bean
CallCredentials basicAuthCredentials() {
    return CallCredentialsHelper.basicAuth("user", "password");
}

@Bean
CallCredentials bearerAuthForwardingCredentials() {
    return CallCredentialsHelper.bearerAuth(() -> KeycloakSecurityContext.getTokenString());
}
````

### 使用 CallCredentials

如果您的应用程序上下文中只有一个`CallCredentials`，我们将自动为您创建一个 `StubTransformer`，并配置到所有的 `Stub`上。 如果您想为每个 Stub 配置不同的凭据，那么您可以使用 [`CallCredentialsHelper`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-starter/latest/net/devh/boot/grpc/client/security/CallCredentialsHelper.html) 中提供的帮助方法。

> **注意:** `StubTransformer` 只能自动配置注入的 `Stub`。 它们无法修改原始的  `Channel`。

您还可以配置 `CallCredentials`(例如用于用户的凭据)：

````java
MyServiceBlockingStub myServiceForUser = myService.withCallCredentials(userCredentials);
return myServiceForUser.send(request);
````

### 重试时使用新的令牌做认证

如果要重试由于令牌过期而失败的调用(使用 grpc 内置重试机制)， 您可以使用 以下示例 `ClientInterceptor` 作为自动向令牌存储器报告失败的指南。 请注意，许多流行的基于令牌的身份验证系统（例如 OAuth）也提供了一个令牌 TTL，可以 用于在第一次发送呼叫之前自动更新令牌，从而使这个功能变得过时。

````java
@Override
public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

    callOptions = callOptions
            .withCallCredentials(this.credentials)
            .withStreamTracerFactory(new ClientStreamTracer.Factory() {

                @Override
                public ClientStreamTracer newClientStreamTracer(
                        ClientStreamTracer.StreamInfo info, Metadata headers) {

                    // Make sure your implementations do _not_ block and return _immediately_
                    final Object authToken = headers.get(AUTH_TOKEN_KEY);
                    return new ClientStreamTracer() {

                        @Override
                        public void streamClosed(final Status status) {
                            this.credentials.invalidate(authToken);
                        }
                    };

                }
            });

    return next.newCall(method, callOptions);
}
````

了解更多详情，请参阅 [重试时如何使用新的认证令牌？](https://github.com/grpc/grpc-java/issues/7345#issuecomment-679295003)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- *安全性*

----------

[<- 返回索引](../index.md)

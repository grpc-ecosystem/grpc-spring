# 服务端安全

[<- 返回索引](../index.md)

本节描述如何使用传输层安全和身份验证来保护您的应用程序。 我们强烈建议至少启用运输层安全。

## 目录 <!-- omit in toc -->

- [启用传输图层安全](#启用传输层安全)
  - [基础要求](#基础要求)
  - [服务端配置](#服务端配置)
- [双向证书认证](#双向证书认证)
- [认证和授权](#认证和授权)
  - [配置身份验证](#配置身份验证)
  - [配置授权](#配置授权)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [服务端事件](events.md)
- *安全性*

## 启用传输图层安全

您可以使用 Spring 的配置机制来配置传输层安全。 关于非安全性相关的配置选项参见 [配置](configuration.md) 页面。

如果你的服务在 TLS 的反向代理后面，你可能不需要设置 `TLS/`。 如果您不熟悉安全，请咨询安全专家。 请不要忘记检查是否存在安全问题。 ^^

> **注意: ** 请参考 [官方文档](https://github.com/grpc/grpc-java/blob/master/SECURITY.md) 以获取更多信息！

### 基础要求

- 在您的 classpath 上有兼容的 `SSL `/`TLS` 实现
  - 包含 [grpc-netty-shaded](https://mvnrepository.com/artifact/io.grpc/grpc-netty-shaded)
  - 对于[`grpc-netty`](https://mvnrepository.com/artifact/io.grpc/grpc-netty)，还需要额外添加 [`nety-tcnative-boringssl-static`](https://mvnrepository.com/artifact/io.netty/netty-tcnative-boringssl-static) 依赖。 (请使用 [grpc-java的 Netty 安全部分](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty) 表中列出**完全相同** (兼容)的版本)。
- 带有私钥的证书

#### 生成一个自签名的证书

如果您没有证书(例如内部测试服务器)，您可以使用`openssl`生成证书：

````sh
openssl req -x509 -nodes -subj "//CN=localhost" -newkey rsa:4096 -sha256 -keyout server.key -out server.crt -days 3650
````

请注意，如果没有额外配置，这些证书不受任何应用程序的信任。 我们建议您使用受全球CA或您公司CA信任的证书。

### 服务端配置

为了允许 grpc-server 使用 `TLS ` 您必须使用以下选项来配置它：

````properties
grpc.server.security.enabled=true
grpc.server.security.certificateChain=file:certificates/server.crt
grpc.server.security.privateKey=file:certificates/server.key
#grpc.server.security.privateKeyPassword=MyStrongPassword
````

如果您想知道这里支持哪些选项，请阅读 [Spring 的 Resource 文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources-resourceloader)。

对于客户端的配置，请参考 [客户端安全](../client/security.md) 页面。

## 双向证书认证

如果您想要确保只有可信的客户端能连接到服务端，您可以启用共同证书进行身份验证。 这允许或强制客户端使用`x509`证书验证自己。

要启用相互身份验证，只需将以下属性添加到您的配置：

````properties
grpc.server.security.trustCertCollection=file:certificates/trusted-clients.crt.collection
grpc.server.security.clientAuth=REQUIRE
````

您可以通过简单地绑定客户端证书创建 `trusted-clients.crt.collection` 文件：

````sh
cat client*.crt > trusted-clients.crt.collection
````

`客户端认证`模式定义了服务端的行为：

- `REQUIRE` 客户端证书必须通过认证。
- `OPTIONAL` 对客户端的证书进行身份验证，但不会强制这么做。

如果您只想保护一些重要的服务或方法，您可以使用 `OPTIONAL`。

尤其是在后一种情况下，适当的配置身份认证尤为重要。

## 认证和授权

`grpc-spring-boot-starter` 原生支持 `spring-security` , 因此您可以使用众所周知的一些注解来保护您的应用程序。

![服务端请求安全](/grpc-spring-boot-starter/assets/images/server-security.svg)

### 配置身份验证

为了支持来自 grpc 客户端的身份验证，您必须定义客户端如何被允许进行身份验证。 您可以通过自定义 [`GrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/GrpcAuthenticationReader.html) 来实现。

grpc-spring-boot-starter 提供了一些内置实现：

- [`AnonymousAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/AnonymousAuthenticationReader.html) Spring 的匿名身份认证。
- [`BasicGrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/BasicGrpcAuthenticationReader.html) 基础身份认证。
- [`BearerAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/BearerAuthenticationReader.html) OAuth 以及类似协议的身份认证。
- [`SSLContextGrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/SSLContextGrpcAuthenticationReader.html) 基于证书的身份认证。
- [`CompositeGrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/CompositeGrpcAuthenticationReader.html) 依次尝试多个身份验证器。

您 Bean 的定义将跟下面这个示例类似：

````java
@Bean
public GrpcAuthenticationReader grpcAuthenticationReader() {
    return new BasicGrpcAuthenticationReader();
}
````

如果您想要强制用户使用 `CompositegrpcAuthenticationReader` ，而其中的一个`GrpcAuthenticationReader` 抛出一个`AuthenticationException`。 那么身份验证将失败，并且停止请求的处理。 如果 `GrpcAuthenticationReader` 返回 null，用户任然是未经验证。 如果身份验证器能够提取凭证/认证，则会交给 Spring 的`AuthenticationManager` 来管理。 由它来决定是否发送有效凭据，并且是否可以继续进行操作。

#### 设置示例

以下部分包含不同身份认证的配置示例：

> 注意：不必在`CompositegrpcAuthenticationReader` 中包装阅读器，你可以直接添加多种机制。

##### 基本认证

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

##### Bearer 认证 (OAuth2/OpenID-Connect)

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
    // The actual token class is dependent on your spring-security library (OAuth2/JWT/...)
    readers.add(new BearerAuthenticationReader(accessToken -> new BearerTokenAuthenticationToken(accessToken)));
    return new CompositeGrpcAuthenticationReader(readers);
}
````

您也可能想要自定义 *GrantedAuthoritiesConverter* 来映射持有者 token 到权限 / 角色到 Spring Security 的 <>GrantedAuthority</code> 中。

##### 证书认证

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

另见[双向证书认证](#mutual-certificate-authentication)。

### 配置授权

这个步骤非常重要，因为它实际保护您的应用程序免受不必要的访问。 您可以通过两种方式保护您的 grpc 服务端。

#### gRPC 安全检查

保护应用程序安全的一种方式是将 [`GrpcSecurityMetadataSource`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/check/GrpcSecurityMetadataSource.html) bean 添加到您的应用商家文中。 它允许您在每个 grpc 方法级别返回安全条件。

一个示例 bean 定义 (使用硬代码规则) 可能如下所示：

````java
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;

@Bean
GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
    final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();
    source.set(MyServiceGrpc.getMethodA(), AccessPredicate.authenticated());
    source.set(MyServiceGrpc.getMethodB(), AccessPredicate.hasRole("ROLE_USER"));
    source.set(MyServiceGrpc.getMethodC(), AccessPredicate.hasAllRole("ROLE_FOO", "ROLE_BAR"));
    source.set(MyServiceGrpc.getMethodD(), (auth, call) -> "admin".equals(auth.getName()));
    source.setDefault(AccessPredicate.denyAll());
    return source;
}

@Bean
AccessDecisionManager accessDecisionManager() {
    final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
    voters.add(new AccessPredicateVoter());
    return new UnanimousBased(voters);
}
````

您必须配置 `AccessDecisionManager` 否则它不知道如何处理`AccessPredicate`。

此方法的好处是您能够将配置移动到外部文件或数据库。 但是你必须自己实现。

#### Spring 注解安全性检查

当然，也可以仅仅使用 Spring Security 的注解。 对于这种情况，您必须将以下注解添加到您的某个 `@Configuration` 类中：

````java
@EnableGlobalMethodSecurity(___Enabled = true, proxyTargetClass = true)
````

> 请注意 `proxyTargetClass = true` 是必需的！ 如果你忘记添加它，你会得到很多 `UNimpleneted`  的响应。 然而，您添加它将收到一个警告，`MyServiceImplic#bindService()` 方法是 final 修饰的。 **不要** 试图取消这些 final 修饰的方法，这将导致安全被绕过。

然后您可以简单地在您的 grpc 方法上加注解：

````java
@Override
@Secured("ROLE_ADMIN")
// MyServiceGrpc.methodX
public void methodX(Request request, StreamObserver<Response> responseObserver) {
    [...]
}
````

> 这个库假定你扩展 `ImplicBase` (由 grpc生成)来实现服务。 不这样做可能会导致绕过 Spring Security 的安全配置。

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [服务端事件](events.md)
- *安全性*

----------

[<- 返回索引](../index.md)

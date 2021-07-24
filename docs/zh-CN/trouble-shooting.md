# 疑难解答

[<- 返回索引](index.md)

本节描述这个项目的一些常见错误，以及如何解决这些错误。 请注意，这个页面永远不能覆盖所有案件，还请搜索现有的 issures/PRs（打开和关闭状态的）。 如果对应的主题已经存在，请给我们留下评论/信息，以便我们知道你也会受到影响。 如果没有这样的主题，请随时打开本页底部描述创建一个的新主题。

## 目录

- [传输失败](#transport-failed)
- [网络因未知原因关闭](#network-closed-for-unknown-reason)
- [找不到 TLS ALPN 提供商](#could-not-find-tls-alpn-provider)
- [证书不匹配](#dismatching-certificates)
- [不受信任的证书](#untrusted-certificates)
- [服务端端口被占用](#server-port-already-in-use)
- [创建 issues / 提问题](#creating-issues)

## 传输失败

### 服务端

````txt
2019-07-07 10:05:46.217  INFO 6552 --- [-worker-ELG-3-5] i.g.n.s.i.g.n.N.connections              : Transport failed

io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2Exception: HTTP/2 client preface string missing or corrupt. Hex dump for received bytes: 16030100820100007e0303aae6126974cbb4638b325d6bdb
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2Exception.connectionError(Http2Exception.java:85) ~[grpc-netty-shaded-1.21.0.jar:1.21.0]
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler$PrefaceDecoder.readClientPrefaceString(Http2ConnectionHandler.java:318) ~[grpc-netty-shaded-1.21.0.jar:1.21.0]
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler$PrefaceDecoder.decode(Http2ConnectionHandler.java:251) ~[grpc-netty-shaded-1.21.0.jar:1.21.0]
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler.decode(Http2ConnectionHandler.java:450) [grpc-netty-shaded-1.21.0.jar:1.21.0]
````

### 客户端

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
    at io.grpc.stub.ClientCalls.toStatusRuntimeException(ClientCalls.java:235)
    at io.grpc.stub.ClientCalls.getUnchecked(ClientCalls.java:216)
    at io.grpc.stub.ClientCalls.blockingUnaryCall(ClientCalls.java:141)
    at net.devh.boot.grpc.examples.lib.SimpleGrpc$SimpleBlockingStub.sayHello(SimpleGrpc.java:178)
    [...]
Caused by: io.grpc.netty.shaded.io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record: 00001204000000000000037fffffff000400100000000600002000000004080000000000000f0001
    at io.grpc.netty.shaded.io.netty.handler.ssl.SslHandler.decodeJdkCompatible(SslHandler.java:1204)
    at io.grpc.netty.shaded.io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1272)
    at io.grpc.netty.shaded.io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:502)
````

### 问题

服务器运行在`PLAINTEXT`模式，但客户端试图在`TLS`(默认)模式中连接它。

### 简单的解决办法

将客户端配置在`PLAINTEXT`模式下连接(不推荐生产)。

添加以下条目到您的客户端应用程序配置：

````properties
grpc.client.__name__.negotiationType=PLAINTEXT
````

### 更好的解决办法

将服务端配置在`TLS`模式下运行(推荐)。

添加以下条目到您的服务端应用程序配置：

````properties
grpc.server.security.enabled=true
grpc.server.security.certificateChain=file:certificates/server.crt
grpc.server.security.privateKey=file:certificates/server.key
````

## 网络因未知原因关闭

### 客户端

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: Network closed for unknown reason
````

### 问题

您可能是 (1) 尝试通过 `TLS ` 模式连接到 grpc-server 时，使用 `PLAINTE` 客户端 或 (2) 目标不是一个 grpc-server （例如 Web 服务）。

### 解决办法

1. 配置您的客户端使用`TLS`模式。

   ````properties
   grpc.client.__name__.negotiationType=TLS
   ````

   或删除`negotiationType`配置，因为默认情况下`TLS`。
2. 使用 `grpcurl` 或类似工具，验证已配置的服务端正在运行的是 grpc 服务

## 找不到 TLS ALPN 提供商

### 服务端

````txt
org.springframework.context.ApplicationContextException: Failed to start bean 'nettyGrpcServerLifecycle'; nested exception is java.lang.IllegalStateException: Could not find TLS ALPN provider; no working netty-tcnative, Conscrypt, or Jetty NPN/ALPN available
    at org.springframework.context.support.DefaultLifecycleProcessor.doStart(DefaultLifecycleProcessor.java:185) ~[spring-context-5.1.8.RELEASE.jar:5.1.8.RELEASE]
    [...]
Caused by: java.lang.IllegalStateException: Could not find TLS ALPN provider; no working netty-tcnative, Conscrypt, or Jetty NPN/ALPN available
    at io.grpc.netty.GrpcSslContexts.defaultSslProvider(GrpcSslContexts.java:258) ~[grpc-netty-1.21.0.jar:1.21.0]
    at io.grpc.netty.GrpcSslContexts.configure(GrpcSslContexts.java:171) ~[grpc-netty-1.21.0.jar:1.21.0]
    at io.grpc.netty.GrpcSslContexts.forServer(GrpcSslContexts.java:130) ~[grpc-netty-1.21.0.jar:1.21.0]
    [...]
````

### 客户端

````txt
[...]
Caused by: java.lang.IllegalStateException: Failed to create channel: <name>
    at net.devh.boot.grpc.client.inject.GrpcClientBeanPostProcessor.processInjectionPoint(GrpcClientBeanPostProcessor.java:118) ~[grpc-client-spring-boot-autoconfigure-2.4.0.RELEASE.jar:2.4.0.RELEASE]
    at net.devh.boot.grpc.client.inject.GrpcClientBeanPostProcessor.postProcessBeforeInitialization(GrpcClientBeanPostProcessor.java:77)
    [...]
Caused by: java.lang.IllegalStateException: Could not find TLS ALPN provider; no working netty-tcnative, Conscrypt, or Jetty NPN/ALPN available
    at io.grpc.netty.GrpcSslContexts.defaultSslProvider(GrpcSslContexts.java:258) ~[grpc-netty-1.21.0.jar:1.21.0]
    at io.grpc.netty.GrpcSslContexts.configure(GrpcSslContexts.java:171) ~[grpc-netty-1.21.0.jar:1.21.0]
    at io.grpc.netty.GrpcSslContexts.forClient(GrpcSslContexts.java:120) ~[grpc-netty-1.21.0.jar:1.21.0]
    [...]
````

### 两端

````txt
AbstractMethodError: io.netty.internal.tcnative.SSL.readFromSSL()
````

### 问题

classpath 上没有 (兼容) netty TLS 实现。

### 解决办法

从[`grpc-netty`](https://mvnrepository.com/artifact/io.grpc/grpc-netty)切换到[`grpc-netty-shaded`](https://mvnrepository.com/artifact/io.grpc/grpc-netty-shaded) 或添加依赖于[`nety-tcnative-boringssl-static`](https://mvnrepository.com/artifact/io.netty/netty-tcnative-boringssl-static) (请使用与[grpc-java 的netty 安全性部分](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty)**完全相同**（兼容的版本）)。

> **注意:** 你需要一个 64 位的 Java 虚拟机。

## 证书不匹配

### 客户端

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
[...]
Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem
[...]
Caused by: java.security.cert.CertificateException: No subject alternative names present
````

或

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
[...]
Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem
[...]
Caused by: java.security.cert.CertificateException: No name matching <name> found
````

### 问题

证书与目标地址/名称不匹配。

### 解决办法

通过在客户端配置中添加以下内容：

````properties
grpc.client.__name__.security.authorityOverride=<authority>
````

## 不受信任的证书

### 客户端

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
[...]
Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem
[...]
Caused by: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
[...]
Caused by: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
````

### 问题

服务器使用的证书不在客户端的信任库中。

### 解决办法

通过使用 java `keytool` 将证书添加到java的信任商店，或配置客户端使用自定义信任的证书文件：

````properties
grpc.client.__name__.security.trustCertCollection=file:certificates/trusted-servers-collection.crt.list
````

> **注意:** 两边的存储库目前在创建时都是只读的，更新不会被应用。

## 服务端端口被占用

### 服务端

````txt
Caused by: java.lang.IllegalStateException: Failed to start the grpc server
    at net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle.start(GrpcServerLifecycle.java:51) ~[grpc-server-spring-boot-autoconfigure-2.4.0.RELEASE.jar:2.4.0.RELEASE]
    [...]
Caused by: java.io.IOException: Failed to bind
    at io.grpc.netty.shaded.io.grpc.netty.NettyServer.start(NettyServer.java:246) ~[grpc-netty-shaded-1.21.0.jar:1.21.0]
    at io.grpc.internal.ServerImpl.start(ServerImpl.java:177) ~[grpc-core-1.21.0.jar:1.21.0]
    at io.grpc.internal.ServerImpl.start(ServerImpl.java:85) ~[grpc-core-1.21.0.jar:1.21.0]
    at net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle.createAndStartGrpcServer(GrpcServerLifecycle.java:90) ~[grpc-server-spring-boot-autoconfigure-2.4.0.RELEASE.jar:2.4.0.RELEASE]
    at net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle.start(GrpcServerLifecycle.java:49) ~[grpc-server-spring-boot-autoconfigure-2.4.0.RELEASE.jar:2.4.0.RELEASE]
    ... 13 common frames omitted
Caused by: java.net.BindException: Address already in use: bind
````

### 问题

grpc 服务端尝试使用的端口被占用。

有四种常见情况可能发生这种错误。

1. 应用程序已在运行
2. 另一个应用程序正在使用该端口
3. grpc 服务器使用了一个已经用于其他用途的端口(例如spring-web)
4. 你正在运行测试，每次测试后你都没有关闭 grpc-server

### 解决办法

1. 尝试使用任务管理器或`jps`搜索应用程序
2. 尝试使用 `netstat` 搜索端口
3. 检查/更改您的配置。 此库默认使用端口 `9090`
4. 添加`@DirtiesContext`到您的测试类和方法中，请注意，这个错误只会从第二次测试开始发生，因此你必须在你的第一个测试类上也加上这个注解！

## 创建 issue

在 GitHub 上创建问题/提问并不难，但你可以稍微努力帮助我们更快地解决您的 个问题。

如果您的问题/疑问一般都是关于 grpc 的问题，请考虑在 [grpc-java](https://github.com/grpc/grpc-java) 上提问。

使用提供的模板来创建新问题，其中包含我们需要的必需/有用信息的部分。

通常来说，你应该在你的问题上包括以下信息：

1. 您有什么类型的诉求？
   - 问题
   - Bug 反馈
   - 功能​​​​​​​​​​​请求
2. 你希望的结果是什么？
3. 问题是什么？ 什么不起作用？ 缺少什么东西，为什么需要？
4. 任何相关堆栈/日志(非常重要)
5. 您使用的是哪个版本？
   - Spring (boot)
   - grpc-java
   - grpc-spring-boot-starter
   - 其他相关库
6. 其他背景
   - 它以前是否正常运行过？
   - 我们如何重现？
   - 有 demo 演示吗？

----------

[<- 返回索引](index.md)

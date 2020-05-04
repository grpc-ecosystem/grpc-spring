# Trouble-Shooting

[<- Back to index](index.md)

This section describes some common errors with this library and grpc in general, and how to solve them.
Please note that this page can never cover all cases, please also search the existing issues/PRs (both opened and closed
ones) for related topics. If a corresponding topic already exists, leave us a comment / info so that we know that you
are also affected. If there is no such topic, feel free to open a new one as described at the bottom of this page.

## Table of contents

- [Transport failed](#transport-failed)
- [Network closed for unknown reason](#network-closed-for-unknown-reason)
- [Could not find TLS ALPN provider](#could-not-find-tls-alpn-provider)
- [Dismatching certificates](#dismatching-certificates)
- [Untrusted certificates](#untrusted-certificates)
- [Server port already in use](#server-port-already-in-use)
- [Client fails to resolve domain name](#client-fails-to-resolve-domain-name)
- [Creating issues / asking questions](#creating-issues)

## Transport failed

### Server-side

````txt
2019-07-07 10:05:46.217  INFO 6552 --- [-worker-ELG-3-5] i.g.n.s.i.g.n.N.connections              : Transport failed

io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2Exception: HTTP/2 client preface string missing or corrupt. Hex dump for received bytes: 16030100820100007e0303aae6126974cbb4638b325d6bdb
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2Exception.connectionError(Http2Exception.java:85) ~[grpc-netty-shaded-1.21.0.jar:1.21.0]
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler$PrefaceDecoder.readClientPrefaceString(Http2ConnectionHandler.java:318) ~[grpc-netty-shaded-1.21.0.jar:1.21.0]
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler$PrefaceDecoder.decode(Http2ConnectionHandler.java:251) ~[grpc-netty-shaded-1.21.0.jar:1.21.0]
    at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler.decode(Http2ConnectionHandler.java:450) [grpc-netty-shaded-1.21.0.jar:1.21.0]
````

### Client-side

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

### The problem

The server runs in `PLAINTEXT` mode, but the client tries to connect it in `TLS` (default) mode.

### The simple solution

a.k.a.: Configure the client to connect in `PLAINTEXT` mode (Not recommended for production).

Add the following entry to your client side application config:

````properties
grpc.client.__name__.negotiationType=PLAINTEXT
````

### The better solution

a.k.a.: Configure the server to run in `TLS` mode (Recommended).

Add the following entry to your sever side application config:

````properties
grpc.server.security.enabled=true
grpc.server.security.certificateChain=file:certificates/server.crt
grpc.server.security.privateKey=file:certificates/server.key
````

## Network closed for unknown reason

### Client-side

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: Network closed for unknown reason
````

### The problem

You are either (1) trying to connect to an grpc-server in `TLS` mode using a `PLAINTEXT` client
or (2) the target is not a grpc-server (e.g. a web-server).

### The solution

1. Configure your client to use `TLS` mode.

   ````properties
   grpc.client.__name__.negotiationType=TLS
   ````

   or remove the `negotiationType` config completely as `TLS` is the default.
2. Validate that the configured server is running and is a grpc-server using `grpcurl` or a similar tool.

## Could not find TLS ALPN provider

### Server-side

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

### Client-side

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

### Both sides

````txt
AbstractMethodError: io.netty.internal.tcnative.SSL.readFromSSL()
````

### The problem

There is no (compatible) netty TLS implementation available on the classpath.

### The solution

Either switch from [`grpc-netty`](https://mvnrepository.com/artifact/io.grpc/grpc-netty) to [`grpc-netty-shaded`](https://mvnrepository.com/artifact/io.grpc/grpc-netty-shaded)
or add a dependency to [`netty-tcnative-boringssl-static`](https://mvnrepository.com/artifact/io.netty/netty-tcnative-boringssl-static)
(Please use the **exact same** (compatible) versions that are listed in the table in [grpc-java's netty security section](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty).

> **Note:** You need a 64bit Java JVM.

## Dismatching certificates

### Client-side

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
[...]
Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem
[...]
Caused by: java.security.cert.CertificateException: No subject alternative names present
````

or

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
[...]
Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem
[...]
Caused by: java.security.cert.CertificateException: No name matching <name> found
````

### The problem

The certificate does not match the target's address/name.

### The solution

Configure an override for the name comparison by adding the following to your client config:

````properties
grpc.client.__name__.security.authorityOverride=<authority>
````

## Untrusted certificates

### Client-side

````txt
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
[...]
Caused by: javax.net.ssl.SSLHandshakeException: General OpenSslEngine problem
[...]
Caused by: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
[...]
Caused by: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
````

### The problem

The certificate used by the server is not in the trust store of the client.

### The solution

Either add the certificate to java's truststore by using java's `keytool`
or configure the client to use a custom trusted certificate file:

````properties
grpc.client.__name__.security.trustCertCollection=file:certificates/trusted-servers-collection.crt.list
````

> **Note:** Both stores are currently read only at creation time and updates won't be picked up.

## Server port already in use

### Server-side

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

### The problem

The port the grpc server is trying to use is already used.

There are four common cases where this error might occur.

1. The application is already running
2. Another application is using that port
3. The grpc server uses a port that is already used for something else (e.g. spring-web)
4. You are running tests and spring didn't shutdown the grpc-server after each test

### The solution

1. Try searching for the application using the task manager or `jps`
2. Try searching for the port using `netstat`
3. Check/Change your configuration. This library uses port `9090` by default
4. Adding `@DirtiesContext` to your test classes and methods
  Please note that the error will only occur from the second test onwards,
  so you have to annotate the first one as well!

## Client fails to resolve domain name

### Client-side

````txt
WARN  io.grpc.internal.ManagedChannelImpl - [Failed to resolve name. status=Status{code=UNAVAILABLE, description=No servers found for `discovery-server:443`}
ERROR n.d.b.g.c.n.DiscoveryClientNameResolver - No servers found for `discovery-server:443`
````

### The problem

The discovery service library or it's configuration failed to specify the scheme how `discovery-server:443` should be
resolved. If you don't have a service discovery, then the default is `dns`, but if you use a discovery service, then
that will be the default and thus failing to resolve that address.

The same applies to other libraries, such as tracing or reporting libraries, which report their results via grpc to an
external server.

### The solution

- Configure the (discovery service) library to specify the `dns` scheme:
  e.g. `dns:///discovery-server:443`
- Search for invocations of `ManagedChannelBuilder#forTarget(String)` or `NettyChannelBuilder#forTarget(String)`
  (or similar methods) and make sure they use the `dns` scheme.
- Disable the service discovery for grpc services:
  `spring.autoconfigure.exclude=net.devh.boot.grpc.client.autoconfigure.GrpcDiscoveryClientAutoConfiguration`
- or create a custom `NameResolverRegistry` bean

See also [client target configuration](client/configuration.md#choosing-the-target).

## Creating issues

Creating issues/asking questions on GitHub isn't hard, but with a little bit of your effort you can help us solving your
issues faster.

If your issue/question is about grpc in general consider asking it over at
[grpc-java](https://github.com/grpc/grpc-java).

Use the provided templates to create new issues, these contain sections for the required/helpful information we need.

In general, you should include the following information in your issue:

1. What type of request do you have?
   - Question
   - Bug-report
   - Feature-Request
2. What do you wish to achieve?
3. What's the problem? What's not working? What's missing and why do you need it?
4. Any relevant stacktraces/logs (very important)
5. Which versions do you use?
   - Spring (boot)
   - grpc-java
   - grpc-spring-boot-starter
   - Other relevant libraries
6. Additional context
   - Did it ever work before?
   - How can we reproduce it?
   - Do you have a demo?

----------

[<- Back to index](index.md)

# Client Security

[<- Back to index](../index)

This page describes how you connect to a grpc server and authenticate yourself.

## Table of Contents <!-- omit in toc -->

- [Enable Transport Layer Security](#enable-transport-layer-security)
  - [Prerequisites](#prerequisites)
- [Disable Transport Layer Security](#disable-transport-layer-security)
  - [Trusting a Server](#trusting-a-server)
- [Mutual Certificate Authentication](#mutual-certificate-authentication)
- [Authentication](#authentication)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started)
- [Configuration](configuration)
- *Security*
- [Tests with Grpc-Stubs](testing)

## Enable Transport Layer Security

Grpc uses `TLS` as the default way to connect to a server so there isn't much else to do.

If you wish to check that you didn't accidentally overwrite the configuration, then check whether the given property
is configured like this or is using its default:

````properties
grpc.client.<SomeName>.negotiationType=TLS
````

For the corresponding server configuration read the [Server Security](../server/security) page.

### Prerequisites

As always there are some simple prerequisites that needs to be met:

- Have a compatible `SSL`/`TLS` implementation on your classpath
  - [grpc-netty-shaded](https://mvnrepository.com/artifact/io.grpc/grpc-netty-shaded) has this included
  - For [`grpc-netty`](https://mvnrepository.com/artifact/io.grpc/grpc-netty) add a dependency to
    [`netty-tcnative-boringssl-static`](https://mvnrepository.com/artifact/io.netty/netty-tcnative-boringssl-static)
    (Please use the **exact same** (compatible) versions that are listed in the table in [grpc-java's netty security section](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty)).

## Disable Transport Layer Security

> **WARNING:** Do NOT do this in production.

Sometimes you don't have the required certificates available (e.g. during development), thus you might you wish to
disable transport layer security, you can do that like this:

````properties
grpc.client.__name__.negotiationType=PLAINTEXT
````

The following example demonstrates how you can configure this property in your tests:

````java
@SpringBootTest(properties = "grpc.client.test.negotiationType=PLAINTEXT")
@SpringJUnitConfig(classes = TestConfig.class)
@DirtiesContext
public class PlaintextSetupTest {

    @GrpcClient("test")
    private MyServiceBlockingStub myService;
````

### Trusting a Server

If you want to trust a server whose certificate is not in the general trust store, or you want to limit which
certificates you trust, you can do so using the following property:

````properties
grpc.client.__name__.security.trustCertCollection=file:trusted-server.crt.collection
````

If you want to know what options are supported here, read
[Spring's Resource docs](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources-resourceloader).

If you use a service identifier, there may be problems with the certificate because it is not valid for the internal service name. In this case you can specify for which name the certificate must be valid:

````properties
grpc.client.__name__.security.authorityOverride=localhost
````

## Mutual Certificate Authentication

In secure environments, you might have to authenticate yourself using a client certificate. This certificate is
usually provided by the server so all you have to do is configure your application to actually use:

````properties
grpc.client.__name__.security.clientAuthEnabled=true
grpc.client.__name__.security.certificateChain=file:certificates/client.crt
grpc.client.__name__.security.privateKey=file:certificates/client.key
````

## Authentication

In addition to mutual certificate authentication, there are several other ways to authenticate yourself, such as
`BasicAuth`.

grpc-spring-boot-starter provides, besides some helper methods, only implementations for BasicAuth. However, there are
various libraries out there that provide implementations for grpc's
[`CallCredentials`](https://grpc.github.io/grpc-java/javadoc/io/grpc/CallCredentials.html).
`CallCredentials` are potentially active components because they can authenticate the request using a (third party)
service and can manage and renew session tokens themselves.

If you have exactly one `CallCredentials` in your application context, we'll automatically create a `StubTransformer`
for you and configure all `Stub`s to use it. If you wish to configure different credentials per stub, then you use our
helper methods in the
[`CallCredentialsHelper`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/security/CallCredentialsHelper.html)
utility.

> **Note:** `StubTransformer`s can only automatically configure injected `Stub`s. They are unable to modify raw
> `Channel`s.

You can also configure the `CallCredentials` just in time (e.g. for user dependent credentials):

````java
MyServiceBlockingStub myServiceForUser = myService.withCallCredentials(userCredentials);
return myServiceForUser.send(request);
````

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started)
- [Configuration](configuration)
- *Security*

----------

[<- Back to index](../index)

# Server Security

[<- Back to Index](../index.md)

This section describes how you secure your application using transport layer security and authentication.
We strongly recommend enabling at least transport layer security.

## Table of Contents <!-- omit in toc -->

- [Enable Transport Layer Security](#enable-transport-layer-security)
  - [Prerequisites](#prerequisites)
  - [Configuring the Server](#configuring-the-server)
- [Mutual Certificate Authentication](#mutual-certificate-authentication)
- [Authentication and Authorization](#authentication-and-authorization)
  - [Configure Authentication](#configure-authentication)
  - [Configure Authorization](#configure-authorization)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Server Events](events.md)
- *Security*

## Enable Transport Layer Security

You can configure transport level security using spring's configuration mechanisms. For non security related
configuration options refer to the [Configuration](configuration.md) page.

If you are behind a reverse proxy that handles TLS for you, you might not need to set up `TLS`. Please consult with
security experts, if you are not familiar with security. Don't forget to check the setup for security issues. ^^

> **Note:** Please refer to the [official documentation](https://github.com/grpc/grpc-java/blob/master/SECURITY.md) for
> additional information!

### Prerequisites

- Have a compatible `SSL`/`TLS` implementation on your classpath
  - [grpc-netty-shaded](https://mvnrepository.com/artifact/io.grpc/grpc-netty-shaded) has this included
  - For [`grpc-netty`](https://mvnrepository.com/artifact/io.grpc/grpc-netty) add a dependency to
    [`netty-tcnative-boringssl-static`](https://mvnrepository.com/artifact/io.netty/netty-tcnative-boringssl-static)
    (Please use the **exact same** (compatible) versions that are listed in the table in [grpc-java's netty security section](https://github.com/grpc/grpc-java/blob/master/SECURITY.md#netty)).
- A certificate with its private key

#### Generating Self Signed Certificates

If you don't have certificates (e.g. for your internal test server) you can generate them using `openssl`:

````sh
openssl req -x509 -nodes -subj "//CN=localhost" -newkey rsa:4096 -sha256 -keyout server.key -out server.crt -days 3650
````

Please note that these certificates aren't trusted by any application without additional configuration.
We recommend that you either use certificates that are trusted by a global CA or your company's CA.

### Configuring the Server

In order to allow the grpc-server to use `TLS` you have to configure it using the following options:

````properties
grpc.server.security.enabled=true
grpc.server.security.certificateChain=file:certificates/server.crt
grpc.server.security.privateKey=file:certificates/server.key
#grpc.server.security.privateKeyPassword=MyStrongPassword
````

If you want to know what options are supported here, read
[Spring's Resource docs](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources-resourceloader).

For the corresponding client configuration read the [Client Security](../client/security.md) page.

## Mutual Certificate Authentication

If you want to make sure that only trustworthy clients can connect to the server you can enable mutual certificate
authentication.
This either allows or forces the client to authenticate itself using a `x509` certificate.

To enable mutual authentication simply add the following properties to your configuration:

````properties
grpc.server.security.trustCertCollection=file:certificates/trusted-clients.crt.collection
grpc.server.security.clientAuth=REQUIRE
````

You can create the `trusted-clients.crt.collection` file by simply concatenating the clients certificates:

````sh
cat client*.crt > trusted-clients.crt.collection
````

The `clientAuth` mode defines how the server will behave:

- `REQUIRE` makes client certificate authentication mandatory.
- `OPTIONAL` will request the client to authenticate itself using a certificate, but won't force it to do so.

You can use `OPTIONAL` if you want to secure only some important services or methods.

Especially in the later case, it is important to configure the authentication appropriately.

## Authentication and Authorization

`grpc-spring-boot-starter` supports `spring-security` natively, so you can just use the well-known annotations to secure
your application.

![server-request-security](/grpc-spring-boot-starter/assets/images/server-security.svg)

### Configure Authentication

In order to support authentication from grpc-clients, you have to define how the clients are allowed to authenticate.
You can do so by defining a
[`GrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/GrpcAuthenticationReader.html).

grpc-spring-boot-starter comes with a number of build-in implementations:

- [`AnonymousAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/AnonymousAuthenticationReader.html)
  for spring's anonymous auth.
- [`BasicGrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/BasicGrpcAuthenticationReader.html)
  for basic auth.
- [`BearerAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/BearerAuthenticationReader.html)
  for OAuth and similar protocols.
- [`SSLContextGrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/SSLContextGrpcAuthenticationReader.html)
  for certificate based authentication.
- [`CompositeGrpcAuthenticationReader`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/authentication/CompositeGrpcAuthenticationReader.html)
  to try multiple readers in order.

Your bean definition will look similar to this example:

````java
@Bean
public GrpcAuthenticationReader grpcAuthenticationReader() {
    return new BasicGrpcAuthenticationReader();
}
````

If you want to force users to authenticate use the `CompositeGrpcAuthenticationReader` and append a
`GrpcAuthenticationReader` that throws a `AuthenticationException`. This marks the authentication as failed and will
stop the processing of the request. If the `GrpcAuthenticationReader` returns null, then the user will continue
unauthenticated. If the reader was able to extract the credentials/authentication, then they will be validated by
spring's `AuthenticationManager`. That instance will then decide, whether the user has sent valid credentials and may
proceed or not.

#### Example setups

The following sections contain example configurations for different authentication setups:

> Note: It is not necessary to wrap the reader in a `CompositeGrpcAuthenticationReader` it should just demonstrate, that
> you could add multiple mechanisms.

##### BasicAuth

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

##### Bearer Authentication (OAuth2/OpenID-Connect)

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

You might also want to define your own *GrantedAuthoritiesConverter* to map the permissions/roles in the bearer token
to Spring Security's `GrantedAuthority`s.

##### Certificate Authentication

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

See also [Mutual Certificate Authentication](#mutual-certificate-authentication).

#### Using AuthenticationManagerResolver

You can also use the `AuthenticationManagerResolver` to dynamically determine the authentication manager to use for
a particular request. This can be useful for applications that support multiple authentication
mechanisms, such as OAuth and OpenID Connect, or that want to delegate authentication to external services.

To use `AuthenticationManagerResolver`, you first need to create a bean that implements
the `AuthenticationManagerResolver<GrpcServerRequest>` interface instead of `AuthenticationManager`. The `resolve()` method of this bean should
return the AuthenticationManager to use for a particular request.

````java
@Bean
AuthenticationManagerResolver<GrpcServerRequest> grpcAuthenticationManagerResolver() {
    return grpcServerRequest -> {
        AuthenticationManager authenticationManager = // Check the grpc request and return an authenticationManager
        return authenticationManager;
    };
}

@Bean
GrpcAuthenticationReader authenticationReader() {
    final List<GrpcAuthenticationReader> readers = new ArrayList<>();
    // The actual token class is dependent on your spring-security library (OAuth2/JWT/...)
    readers.add(new BearerAuthenticationReader(accessToken -> new BearerTokenAuthenticationToken(accessToken)));
    return new CompositeGrpcAuthenticationReader(readers);
}
````

### Configure Authorization

This step is very important as it actually secures your application against unwanted access. You can secure your
grpc-server in two ways.

#### gRPC security checks

One way to secure your application is adding
[`GrpcSecurityMetadataSource`](https://javadoc.io/page/net.devh/grpc-server-spring-boot-autoconfigure/latest/net/devh/boot/grpc/server/security/check/GrpcSecurityMetadataSource.html)
bean to your application context. It allows you to return the security conditions on a per grpc method level.

An example bean definition (using hard coded rules) might look like this:

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

You have to configure the `AccessDecisionManager` otherwise it doesn't know how to deal with the `AccessPredicate`s.

This approach has the benefit that you are able to move the configuration to an external file or database.
You have to implement that yourself though.

#### Spring annotation security checks

Of course, it is also possible to just use spring-security's annotations.
For this use case you have to add the following annotation to one of your `@Configuration` classes:

````java
@EnableMethodSecurity(proxyTargetClass = true)
````

> Please note that `proxyTargetClass = true` is required! If you forget to add it, you will get a lot of `UNIMPLEMENTED`
> responses. However, you will receive a warning that `MyServiceImpl#bindService()` method is final. Do **NOT** try to
> un-final that method as that would bypass security.

Then you can simply annotate the grpc method implementation:

````java
@Override
@Secured("ROLE_ADMIN")
// MyServiceGrpc.methodX
public void methodX(Request request, StreamObserver<Response> responseObserver) {
    [...]
}
````

> This library assumes that you extend the `ImplBase` (generated by grpc) in order to implement the service. Not doing
> so might result in bypassing spring-security.

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Server Events](events.md)
- *Security*

----------

[<- Back to Index](../index.md)

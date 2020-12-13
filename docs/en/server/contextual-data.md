# Contextual Data / Scoped Beans

[<- Back to Index](../index.md)

This section describes how you can store contextual / per request data.

## Table of Contents <!-- omit in toc -->

- [A Word of Warning](#a-word-of-warning)
- [grpcRequest Scope](#grpcrequest-scope)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Exception Handling](exception-handling.md)
- *Contextual Data / Scoped Beans*
- [Testing the Service](testing.md)
- [Security](security.md)

## A Word of Warning

In grpc-java different steps in the message delivery / request process can and will run in different threads. This is
not only but especially relevant for streaming calls. Avoid using `ThreadLocal`s inside your `ServerInterceptor`s and
grpc service method implementations (in the entire grpc context). When it comes down to it, the preparation phase, every
single message and the completion phase might run in different threads. If you wish to store data for the duration of
the session, do so either using grpc's `Context` or `grpcRequest` scoped beans.

## grpcRequest Scope

This library adds a `grpcRequest` that works similar to spring web's `request` scope. It is only valid for a single
request.

First you define your bean with the `@Scope` annotation:

````java
@Bean
@Scope(scopeName = "grpcRequest", proxyMode = ScopedProxyMode.TARGET_CLASS)
//@Scope(scopeName = GrpcRequestScope.GRPC_REQUEST_SCOPE_NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
ScopedBean myScopedBean() {
    return new ScopedBean();
}
````

> The `proxyMode = TARGET_CLASS` is required unless you only use the bean inside another `grpcRequest` scoped bean.
> Please note that this `proxyMode` does not work with final classes or methods.

After that, you can use the bean as you are used to:

````java
@Autowired
private ScopedBean myScopedBean;

@Override
public void grpcMethod(Request request, StreamObserver<Response> responseObserver) {
    responseObserver.onNext(myScopedBean.magic(request));
    responseObserver.onCompleted();
}
````

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Exception Handling](exception-handling.md)
- *Contextual Data / Scoped Beans*
- [Testing the Service](testing.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

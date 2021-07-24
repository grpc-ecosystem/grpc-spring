# Exception Handling inside GrpcService

[<- Back to Index](../index.md)

This section describes how you can handle exceptions inside GrpcService layer without cluttering up your code.

## Table of Contents <!-- omit in toc -->

- [Proper exception handling](#proper-exception-handling)
- [Detailed explanation](#detailed-explanation)
  - [Priority of mapped exceptions](#priority-of-mapped-exceptions)
  - [Sending Metadata in response](#sending-metadata-in-response)
  - [Overview of returnable types](#overview-of-returnable-types)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Contextual Data](contextual-data.md)
- *Exception Handling*
- [Testing the Service](testing.md)
- [Security](security.md)

## Proper exception handling

If you are already familiar with spring's [error handling](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-error-handling),
you should see some similarities with the exception handling for gRPC.

_An explanation for the following class:_

```java
@GrpcAdvice
public class GrpcExceptionAdvice {


    @GrpcExceptionHandler
    public Status handleInvalidArgument(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT.withDescription("Your description").withCause(e);
    }

    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
        Status status = Status.NOT_FOUND.withDescription("Your description").withCause(e);
        Metadata metadata = ...
        return status.asException(metadata);
    }

}
```

- `@GrpcAdvice` marks a class to be checked up for exception handling methods
- `@GrpcExceptionHandler` marks the annotated method to be executed, in case of the _specified_ exception being thrown
  - f.e. if your application throws `IllegalArgumentException`,
    then the `handleInvalidArgument(IllegalArgumentException e)` method will be executed
- The method must either return a `io.grpc.Status`, `StatusException`, or `StatusRuntimeException`
- If you handle server errors, you might want to log the exception/stacktrace inside the exception handler

> **Note:** Cause is not transmitted from server to client - as stated in [official docs](https://grpc.github.io/grpc-java/javadoc/io/grpc/Status.html#withCause-java.lang.Throwable-)
> So we recommend adding it to the `Status`/`StatusException` to avoid the loss of information on the server side.

## Detailed explanation

### Priority of mapped exceptions

Given this method with specified exception in the annotation *and* as a method argument

```java
@GrpcExceptionHandler(ResourceNotFoundException.class)
public StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
    // your exception handling
}
```

If the `GrpcExceptionHandler` annotation contains at least one exception type, then only those will be
considered for exception handling for that method. The method parameters must be "compatible" with the specified
exception types. If the annotation does not specify any handled exception types, then all method parameters are being
used instead.

_("Compatible" means that the exception type in annotation is either the same class or a superclass of one of the
listed method parameters)_

### Sending Metadata in response

In case you want to send metadata in your exception response, let's have a look at the following example.

```java
@GrpcExceptionHandler
public StatusRuntimeException handleResourceNotFoundException(IllegalArgumentException e) {
    Status status = Status.INVALID_ARGUMENT.withDescription("Your description");
    Metadata metadata = ...
    return status.asRuntimeException(metadata);
}
```

If you do not need `Metadata` in your response, just return your specified `Status`.

### Overview of returnable types

Here is a small overview of possible mapped return types with `@GrpcExceptionHandler` and if custom `Metadata` can be
returned:

| Return Type | Supports Custom Metadata |
| ----------- | --------------- |
| `Status` | &cross; |
| `StatusException` | &#10004; |
| `StatusRuntimeException` | &#10004; |

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Contextual Data](contextual-data.md)
- *Exception Handling*
- [Testing the Service](testing.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

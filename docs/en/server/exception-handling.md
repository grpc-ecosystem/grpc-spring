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

If you are already familiar with springs [error handling](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-error-handling),
you should see some similarity with intended exception handling for gRPC.


_An explanation for the following class:_
```java
@GrpcAdvice
public class GrpcExceptionAdvice {


    @GrpcExceptionHandler
    public Status handleInvalidArgument(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT.withDescription("Your description");
    }

    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
        Status status = Status.NOT_FOUND.withDescription("Your description");
        Metadata metadata = ...
        return status.asException(metadata);
    }

}
```

- `@GrpcAdvice` marks a class to be picked up for exception handling
- `@GrpcExceptionHandler` maps given method to be executed, in case of _specified_ thrown exception
    - f.e. if your application throws `IllegalArgumentException`, then the `handleInvalidArgument(IllegalArgumentException e)` method will be is executed
- `io.grpc.Status` is specified and returned response status

> **Note:** Cause is not transmitted from server to client - as stated in [official docs](https://grpc.github.io/grpc-java/javadoc/io/grpc/Status.html#withCause-java.lang.Throwable-)

## Detailed explanation

### Priority of mapped exceptions

Given method with specified Exception in Annotation *and* as method argument

```java
    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
        // your exception handling
    }
```
> **Note:** Exception type in annotation is prioritized in mapping the exception over listed method argument
> **and** they _must_ match the types declared with this value.

_(Matching means: Exception type in annotation is superclass of listed method parameter)_

If no annotation type is provided in the annotation, listed method parameter are being picked up.

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

As you do not need `Metadata` in your response, just return your specified `Status`.

### Overview of returnable types

Here is a small overview of possible mapped return types with `@GrpcExceptionHandler` and if
custom Metadata can be returned.

| Return Type | Custom Metadata |
| ----------- | --------------- |
| Status | &cross; |
| StatusException | &#10004; |
| StatusRuntimeException | &#10004; |
| Throwable | &cross; |

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Contextual Data](contextual-data.md)
- *Exception Handling*
- [Testing the Service](testing.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

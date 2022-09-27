# GrpcService 中的异常处理

[<- 返回索引](../index.md)

本节介绍如何处理 `GrpcService` 层内的异常，类似 `spring-boot-web` 中的统一异常处理。

## 目录 <!-- omit in toc -->

- [正确的异常处理](#proper-exception-handling)
- [详细解释](#detailed-explanation)
  - [映射异常的优先级](#priority-of-mapped-exceptions)
  - [发送元数据作为响应](#sending-metadata-in-response)
  - [可返回类型概述](#overview-of-returnable-types)

## 其他主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- *异常处理*
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [安全性](security.md)

## 正确的异常处理

如果您已经熟悉 spring 的 [错误处理](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-error-handling)，您应该会看到与 gRPC 的异常处理有一些相似之处。

_示例:_

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

- `@GrpcAdvice` 作用于配置类，开启拦截实现统一异常处理
- `@GrpcExceptionHandler` 作用于方法，拦截指定异常进行处理
  - 解释：当程序抛出 `IllegalArgumentException` 时，`handleInvalidArgument(IllegalArgumentException e)` 方法将被执行
- 方法的返回值必须是：`io.grpc.Status`、`StatusException`、`StatusRuntimeException`
- 在统一异常拦截中进行处理异常的堆栈信息，才是正确且优雅的做法

> **注意:**
> - `.withCause(e)`中的信息不会传递给客户端，详细原因见 [官方文档](https://grpc.github.io/grpc-java/javadoc/io/grpc/Status.html#withCause-java.lang.Throwable-)
> - `.withDescription("Your description")`中的信息可以传递到客户端

## 详细解释

### 映射异常的优先级

```java
@GrpcExceptionHandler(ResourceNotFoundException.class)
public StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
    // your exception handling
}
```

在`@GrpcExceptionHandler()`注解中指定异常拦截类型，若未指定具体值，将拦截所有异常
- 支持超类、超接口
- 支持多个值

### 发送元数据作为响应

如果您想在异常响应中发送元数据，可以参考以下示例：

```java
@GrpcExceptionHandler
public StatusRuntimeException handleResourceNotFoundException(IllegalArgumentException e) {
    Status status = Status.INVALID_ARGUMENT.withDescription("Your description");
    Metadata metadata = ...
    return status.asRuntimeException(metadata);
}
```

如果您不需要 `Metadata` response，只需要返回指定的 `Status`

## 其他主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- *异常处理*
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [安全性](security.md)

----------

[<- 返回索引](../index.md)

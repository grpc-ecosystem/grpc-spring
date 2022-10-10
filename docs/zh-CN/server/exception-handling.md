# GrpcService 中的 Exception 处理

[<- 返回索引](../index.md)

本节将介绍如何 GrpcService 中的 Exception，并且不会使你的代码杂乱无章。

## 目录 <!-- omit in toc -->

- [异常处理](#异常处理)
- [详细说明](#详细说明)
  - [异常处理的优先级](#异常处理的优先级)
  - [响应中发送 Metadata](#响应中发送 Metadata)
  - [返回值类型概览](#返回值类型概览)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [上下文数据](contextual-data.md)
- *异常处理*
- [测试服务](testing.md)
- [服务端事件](events.md)
- [安全性](security.md)

## 异常处理

如果你熟悉 Spring 的 [异常处理](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-error-handling)， 你应该能看到 gRPC 异常处理与它非常相似。

_如下所示：_

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

- 在类中使用 `@GrpcAdvice` 注解
- 使用 `@GrpcExceptionHandler` 标记_指定的_ Exception，在抛出指定的 Exception 时，方法将被执行。
  - 例如： 如果您的应用程序抛出 `IllegalArgumentException` 异常，那么 `handleInvalidArgument(IllegalArgumentException e)` 方法将会被执行
- 方法必须返回 `io.grpc.Status`, `StatusException` 或 `StatusRuntimeException`
- 如果你处理服务端错误，你可能想要在异常处理程序中记录异常/堆栈跟踪

> **注意：** 原因不会从服务器传送到客户端 - 如 [官方文档](https://grpc.github.io/grpc-java/javadoc/io/grpc/Status.html#withCause-java.lang.Throwable-) 所述。因此我们建议将其添加到 `状态`/`状态异常` 以避免在服务端丢失异常信息。

## 详细说明

### 异常处理的优先级

在注解中指定的异常类型跟方法参数中的异常类型，他们中间是 *and* 的关系。

```java
@GrpcExceptionHandler(ResourceNotFoundException.class)
public StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
    // your exception handling
}
```

如果 `GrpcExceptionHandler` 注解包含至少一个异常类型，那么该方法的异常处理将只会处理这些异常类型。 方法参数中的类型必须与指定的异常类型 "兼容" 如果注解中没有指定任何处理的异常类型，那么所有的方法参数都会被使用。

_("兼容"是指注解中的异常类型是 列出方法参数之一的同一个类或父类)_

### 响应中发送 Metadata

如果你想要在异常响应中发送 metadata，可以看看下面的例子。

```java
@GrpcExceptionHandler
public StatusRuntimeException handleResourceNotFoundException(IllegalArgumentException e) {
    Status status = Status.INVALID_ARGUMENT.withDescription("Your description");
    Metadata metadata = ...
    return status.asRuntimeException(metadata);
}
```

如果您的响应不需要 `Metadata` ，只需返回您指定的 `Status`。

### 返回值类型概览

下面是关于 `@GrpcExceptionHandler` 可能的返回类型和是否支持自定义 `Metadata` 的概览。

| 返回值类型                    | 支持自定义元数据 |
| ------------------------ | -------- |
| `Status`                 | &cross;  |
| `StatusException`        | &#10004; |
| `StatusRuntimeException` | &#10004; |

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [上下文数据](contextual-data.md)
- *异常处理*
- [测试服务](testing.md)
- [服务端事件](events.md)
- [安全性](security.md)

----------

[<- 返回索引](../index.md)

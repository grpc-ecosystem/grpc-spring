# 上下文数据 / Bean 的作用域

[<- index返回索引](../index)

本节描述您如何保存请求上下文数据 / 每个请求的数据。

## 目录 <!-- omit in toc -->

- [警告语](#a-word-of-warning)
- [grpcRequest 作用域](#grpcrequest-scope)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started)
- [配置](configuration)
- *上下文数据 / Bean 的作用域*
- [测试服务](testing)
- [安全性](security)

## 警告语

在 grpc-java 中，消息发送 / 请求处理中的不同阶段可能在不同的线程中运行。 流式调用中也是这样。 避免在您的`ServerIntercetor` 和 grpc 服务方法实现中(在整个 gRPC 上下文中)使用 `ThreadLocal`。 When it comes down to it, the preparation phase, every single message and the completion phase might run in different threads. 如果您想要在会话中存储数据，请使用 grpc 的 `Context` 或 `grpcRequest` 作用域。

## grpcRequest 作用域

该项目添加了一个`grpcRequest`，该功能类似于 Spring Web 的`request` 作用域。 它只适用于单个的请求。

首先需要用 `@Scope` 注解定义 Bean：

````java
@Bean
@Scope(scopeName = "grpcRequest", proxyMode = ScopedProxyMode.TARGET_CLASS)
//@Scope(scopeName = GrpcRequestScope.GRPC_REQUEST_SCOPE_NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
ScopedBean myScopedBean() {
    return new ScopedBean();
}
````

> `proxyMode = TARGET_CLASS` 是必须的，除非在另一个 `grpcRequest` 作用域中配置了它. 请注意，这个`proxyMode` 不适用于 final 修饰的类和方法。

之后，您就可以像以前那样使用 Bean：

````java
@Autowired
private ScopedBean myScopedBean;

@Override
public void grpcMethod(Request request, StreamObserver<Response> responseObserver) {
    responseObserver.onNext(myScopedBean.magic(request));
    responseObserver.onCompleted();
}
````

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started)
- [配置](configuration)
- *上下文数据 / Bean 的作用域*
- [测试服务](testing)
- [安全性](security)

----------

[<- index返回索引](../index)

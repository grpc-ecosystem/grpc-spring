# 服务端事件

[<- 返回索引](../index.md)

本节介绍如何订阅 grpc 服务端相关的事件。

## 目录 <!-- omit in toc -->

- [事件概览](#event-overview)
  - [GrpcServerLifecycleEvent](#grpcserverlifecycleevent)
  - [GrpcServerStartedEvent](#grpcserverstartedevent)
  - [GrpcServerShutdownEvent](#grpcservershutdownevent)
  - [GrpcServerTerminatedEvent](#grpcserverterminatedevent)
- [订阅事件](#subscribing-to-events)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- *服务端事件*
- [安全性](security.md)

## 事件概览

### GrpcServerLifecycleEvent

与`GrpcServerLifecycle`变化有关的所有事件的抽象基类。

### GrpcServerStartedEvent

此事件将在服务端启动后触发。

### GrpcServerShutdownEvent

此事件将在服务端关闭前触发。 服务端将不再处理新请求。

### GrpcServerTerminatedEvent

此事件将在服务端关闭后触发。 服务端将不再处理任何请求。

## 订阅事件

为了订阅这些事件，你只需在你任何 `@Component` 中的 public 方法上使用 `@EventListener` 注解。

````java
@Component
public class MyEventListenerComponent {

    @EventListener
    public void onServerStarted(GrpcServerStartedEvent event) {
        System.out.println("gRPC Server started, listening on address: " + event.getAddress() + ", port: " + event.getPort());
    }

}
````

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- *服务端事件*
- [安全性](security.md)

----------

[<- 返回索引](../index.md)

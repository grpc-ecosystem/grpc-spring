# gRPC-Java 类似风格的库

除了 [grpc-java](https://github.com/grpc/grpc-java/), 该库还支持其他基于 java 的 grpc 实现.

| 库                                                                                          | 服务端 | 客户端 |
| ------------------------------------------------------------------------------------------ | --- | --- |
| [grpc-java](https://github.com/grpc/grpc-java/)                                            | ✔️  | ✔️  |
| [Reactive gRPC (Reactor)](https://github.com/salesforce/reactive-grpc/tree/master/reactor) | ✔️  | ✏️  |
| [Reactive gRPC (RxJava)](https://github.com/salesforce/reactive-grpc/tree/master/rx-java)  | ✔️  | ✏️  |
| [grpc-kotlin](https://github.com/grpc/grpc-kotlin)                                         | ✔️  | ✏️  |
| [ScalaPB](https://scalapb.github.io/grpc.html)                                             | ✔️  | ✏️  |
| [akka-grpc](https://github.com/akka/akka-grpc)                                             | ✔️  | ✏️  |
| ...                                                                                        | ✔️  | ✏️  |

*✔️ = 内置支持* | *✏️ = 需要自定义*

> **注意：** 您可能需要引入额外的依赖库，这取决于您所用的库。

## 服务端

服务器端应该在没有任何额外配置的情况下工作。 只需要在 `BindableService` 的实现类上增加 `@GrpcService` 注解。

参阅：

- [服务端 - 入门](server/getting-started.md)

## 客户端

客户端对每种类型的 stub 都需要一个 `StubFactory` 。

该库默认提供以下几种 stub 的 工厂 bean：

- gRPC-Java
  - `AbstractAsyncStub` -> `AsyncStubFactory`
  - `AbstractBlockingStub` -> `BlockingStubFactory`
  - `AbstractFutureStub` -> `FutureStubFactory`

请告知我们不支持的 stub 类型，我们可以添加对它们的支持.

参阅：

- [客户端-入门](client/getting-started.md)
- [客户端 - 配置 - StubFactory](client/configuration.md#stubfactory)

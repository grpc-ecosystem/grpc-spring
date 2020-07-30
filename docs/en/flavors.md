# gRPC-Java Flavors

Aside from [grpc-java](https://github.com/grpc/grpc-java/), this library also supports other java based
grpc-implementations.

| Flavor | Server | Client |
| ---  | --- | --- |
| [grpc-java](https://github.com/grpc/grpc-java/) | ✔️ | ✔️ |
| [Reactive gRPC (Reactor)](https://github.com/salesforce/reactive-grpc/tree/master/reactor) | ✔️ | ✏️ |
| [Reactive gRPC (RxJava)](https://github.com/salesforce/reactive-grpc/tree/master/rx-java) | ✔️ | ✏️ |
| [grpc-kotlin](https://github.com/grpc/grpc-kotlin) | ✔️ | ✏️ |
| [ScalaPB](https://scalapb.github.io/grpc.html) | ✔️ | ✏️ |
| [akka-grpc](https://github.com/akka/akka-grpc) | ✔️ | ✏️ |
| ... | ✔️ | ✏️ |

*✔️ = Build-in support* |
*✏️ = Requires customization*

> **Note:** You might require additional dependencies depending on your grpc java flavor.

## Server side

The server side should work without any additional configuration. Just annotatate your implementation of the generated
`BindableService` class with `@GrpcService` and it will be picked up automatically.

See also:

- [Server - Getting Started](server/getting-started.md)

## Client side

The client side requires a `StubFactory` for each type of stub.

This library ships the following stub factory beans by default:

- gRPC-Java
  - `AbstractAsyncStub` -> `AsyncStubFactory`
  - `AbstractBlockingStub` -> `BlockingStubFactory`
  - `AbstractFutureStub` -> `FutureStubFactory`

Please report missing stub types so that we can add support for them.

See also:

- [Client - Getting Started](client/getting-started.md)
- [Client - Configuration - StubFactory](client/configuration.md#stubfactory)

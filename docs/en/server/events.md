# Server Events

[<- Back to Index](../index.md)

This section describes how you can subscribe to events related to the grpc server.

## Table of Contents <!-- omit in toc -->

- [A Word of Warning](#a-word-of-warning)
- [grpcRequest Scope](#grpcrequest-scope)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- *Server Events*
- [Security](security.md)

## Event Overview

### GrpcServerLifecycleEvent

Abstract base class for all events related to `GrpcServerLifecycle` changes.

### GrpcServerStartedEvent

This event will be fired after the server has been started.

### GrpcServerShutdownEvent

This event will be fired before the server starts to shutdown. The server will no longer process new requests.

### GrpcServerTerminatedEvent

This event will be fired after the server completed to shutdown. The server will no longer process requests.

## Subscribing to Events

In order to subscribe to any of these events you can just use the `@EventListener` annotation on a public method
in any of your `@Component`s.

````java
@Component
public class MyEventListenerComponent {

    @EventListener
    public void onServerStarted(GrpcServerStartedEvent event) {
        System.out.println("gRPC Server started, listening on address: " + event.getAddress() + ", port: " + event.getPort());
    }

}
````

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- *Server Events*
- [Security](security.md)

----------

[<- Back to Index](../index.md)

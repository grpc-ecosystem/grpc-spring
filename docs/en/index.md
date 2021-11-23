# gRPC-Spring-Boot-Starter Documentation

gRPC-spring-boot-starter combines [google's open-source high performance RPC-framework](https://grpc.io) with
[spring boot's ease of setup](https://spring.io/projects/spring-boot).
This project simplifies the gRPC-server/client setup to adding one dependency to your project and adding a single
annotation to your service class / client (stub) field.
The features of this library are meant to complement your experience with gRPC and still allow you to do any
customization you need for your project.

## Table of Contents

- Server
  - [Getting Started](server/getting-started.md)
  - [Configuration](server/configuration.md)
  - [Exception Handling](server/exception-handling.md)
  - [Contextual Data / Scoped Beans](server/contextual-data.md)
  - [Testing the Service](server/testing.md)
  - [Server Events](server/events.md)
  - [Security](server/security.md)
- Client
  - [Getting Started](client/getting-started.md)
  - [Configuration](client/configuration.md)
  - [Security](client/security.md)
  - [Tests with Grpc-Stubs](client/testing.md)
- Others setups
- [Trouble-Shooting](trouble-shooting.md)
- [Example Projects](examples.md)
- [gRPC-Java Flavors](flavors.md)
- [Version Overview](versions.md)
- [Spring Boot Actuator / Metrics Support](actuator.md)
- [Brave-Tracing / Spring Cloud Sleuth Support](brave.md)
- [Kubernetes Setup](kubernetes.md)
- [Benchmarking](benchmarking.md)
- [Contributing](contributions.md)

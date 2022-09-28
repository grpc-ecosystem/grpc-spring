# gRPC-Spring-Boot-Starter 文档

gRPC-Spring-Boot-Starter 将 [Google的开源高性能RPC框架](https://grpc.io) 与 [Spring Boot 进行整合](https://spring.io/projects/spring-boot) 该项目简化了 gRPC 服务端 / 客户端的设置，只需要为项目添加了一个依赖项，并在服务实现类 / 客户 (stub) 字段上添加了一个的注解。 这个项目提供的特性仍然能复用您使用 gRPC 的经验，并且允许您执行任何自定义操作。

## 目录

- 服务端
  - [入门指南](server/getting-started.md)
  - [配置](server/configuration.md)
  - [Exception Handling](server/exception-handling.md)
  - [Contextual Data / Scoped Beans](server/contextual-data.md)
  - [Testing the Service](server/testing.md)
  - [Server Events](server/events.md)
  - [Security](server/security.md)
- 客户端
  - [入门指南](client/getting-started.md)
  - [配置](client/configuration.md)
  - [安全性](client/security.md)
  - [使用 Grpc-Stubs 测试](client/testing.md)
- 其他设置
- [疑难解答](trouble-shooting.md)
- [示例项目](examples.md)
- [gRPC-Java Flavors](flavors.md)
- [Version Overview](versions.md)
- [Spring Boot Actuator / Metrics Support](actuator.md)
- [Brave-Tracing / Spring Cloud Sleuth Support](brave.md)
- [Kubernetes Setup](kubernetes.md)
- [Benchmarking](benchmarking.md)
- [Contributing](contributions.md)

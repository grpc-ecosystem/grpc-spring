# gRPC-Spring-Boot-Starter 文档

gRPC-Spring-Boot-Starter 将 [Google的开源高性能RPC框架](https://grpc.io) 与 [Spring Boot 进行整合](https://spring.io/projects/spring-boot) 该项目简化了 gRPC 服务端 / 客户端的设置，只需要为项目添加了一个依赖项，并在服务实现类 / 客户 (stub) 字段上添加了一个的注解。 这个项目提供的特性仍然能复用您使用 gRPC 的经验，并且允许您执行任何自定义操作。

## 目录

- 服务端
  - [入门指南](server/getting-started)
  - [配置](server/configuration)
  - [上下文数据 / Bean 的作用域](server/contextual-data)
  - [测试服务](server/testing)
  - [安全性](server/security)
- 客户端
  - [入门指南](client/getting-started)
  - [配置](client/configuration)
  - [安全性](client/security)
- 其他设置
- [疑难解答](trouble-shooting)
- [示例项目](examples)
- [版本概述](versions)
- [支持 Spring Boot Actuator / Metrics](actuator)
- [支持 Brave-Tracing / Spring Cloud Sleuth](brave)
- [基准测试](benchmarking)
- [参与贡献](contributions)

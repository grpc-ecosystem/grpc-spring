# 示例

示例项目演示如何使用这些项目。

这些项目可以作为您自己的项目的模板。 我们使用它们来验证这个库在不同的环境中运行，我们不会在不受注意的情况下改变它的行为。

> **注意:** 如果您对这些项目有疑问，或者想要其他的示例，随时可以提出一个 [issue](https://github.com/yidongnan/grpc-spring-boot-starter/issues)。

## 本地示例

最简单的设置，使用本地服务端和一个或多个客户端

- [服务端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/local-grpc-server)
- [客户端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/local-grpc-client)
- [说明](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples#local-mode)

## Cloud 示例

使用 eureka 服务发现的 Cloud 环境。

- [Eureka 服务](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-eureka-server)
- [服务端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-grpc-server)
- [客户端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-grpc-client)
- [说明](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples#cloud-mode)

## Cloud 示例

使用 nacos 服务发现的 Cloud 环境。

- [服务端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-grpc-server-nacos)
- [客户端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-grpc-client-nacos)
- [说明](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples#cloud-mode)

## 基础认证示例

演示了 grpc 跟 spring security 的设置。 为了简单起见，此设置使用 Basic 身份验证，但也可以为其使用其他身份验证机制。

- [服务端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/security-grpc-server)
- [客户端](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/security-grpc-client)
- [说明](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples#with-basic-auth-security)

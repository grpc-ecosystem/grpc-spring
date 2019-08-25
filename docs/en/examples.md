# Examples

The example projects demonstrate how to use the projects.

These projects work as is and could be used as templates for your own projects.
We use them to verify that this library works in different environments, and we don't break anything unnoticed.

> **Note:** If you have questions regarding these sample projects or would like an example for another use case,
> feel free to open an [issue](https://github.com/yidongnan/grpc-spring-boot-starter/issues).

## Local example

The simplest setup of all, using a local server and one or more clients.

- [Server](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/local-grpc-server)
- [Client](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/local-grpc-client)
- [Instructions](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples#local-mode)

## Cloud example

A setup for cloud environments using a eureka service discovery service.

- [Eureka-Server](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-eureka-server)
- [Server](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-grpc-server)
- [Client](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/cloud-grpc-client)
- [Instructions](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples#cloud-mode)

## Basic auth example

A setup that demonstrates the usage of spring-security with grpc.
This setup uses basic auth for simplicity reasons, but other authentication mechanism can be used for this as well.

- [Server](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/security-grpc-server)
- [Client](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples/security-grpc-client)
- [Instructions](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/examples#with-basic-auth-security)

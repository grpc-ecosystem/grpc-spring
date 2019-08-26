# Brave / Sleuth Support

[<- Back to index](index)

This page focuses on the integration with [brave](https://github.com/openzipkin/brave) /
[Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth). This is an optional feature.

## Table of contents <!-- omit in toc -->

- [Dependencies](#dependencies)
  - [Brave](#brave)
  - [Spring Cloud Sleuth](#spring-cloud-sleuth)
- [Opt-Out](#opt-out)
- [Additional Notes](#additional-notes)

## Dependencies

grpc-spring-boot-starter provides automatic support for `Brave Instrumentation: GRPC`.
However, there are two requirements:

1. You need the dependencies for Brave on the classpath.
2. You need a `Tracing` bean in your application context.
   *If you have Spring Cloud Sleuth in your classpath, it will automatically configure this bean for you.*

### Brave

You can add the required dependencies for brave to Maven like this:

````xml
<dependency>
    <groupId>io.zipkin.brave</groupId>
    <artifactId>brave-instrumentation-grpc</artifactId>
</dependency>
````

and to Gradle like this:

````groovy
compile("io.zipkin.brave:brave-instrumentation-grpc")
````

### Spring Cloud Sleuth

You can add sleuth to your application using Maven like this:

````xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
````

and using Gradle like this:

````groovy
compile("org.springframework.cloud:spring-cloud-starter-sleuth")
````

Please refer to the [official documentation](https://spring.io/projects/spring-cloud-sleuth) on how to set up/configure
Sleuth.

## Opt-Out

You can opt-out from the grpc-tracing using the following property:

````property
spring.sleuth.grpc.enabled=false
````

## Additional Notes

Spring-Cloud-Sleuth provides a few classes such as
[`SpringAwareManagedChannelBuilder`](https://javadoc.io/page/org.springframework.cloud/spring-cloud-sleuth-core/latest/org/springframework/cloud/sleuth/instrument/grpc/SpringAwareManagedChannelBuilder.html),
those classes solely exists for compatibility reasons with a different library. Do not use them with this library.
grpc-spring-boot-starter provides the same/extended functionality out of the box with the
[`GrpcChannelFactory`](https://javadoc.io/page/net.devh/grpc-client-spring-boot-autoconfigure/latest/net/devh/boot/grpc/client/channelfactory/GrpcChannelFactory.html)
and related classes. See also
[sleuth's javadoc note](https://github.com/spring-cloud/spring-cloud-sleuth/blob/59216c32f7848ec337fb68d1dbec8e87eeb6bf59/spring-cloud-sleuth-core/src/main/java/org/springframework/cloud/sleuth/instrument/grpc/SpringAwareManagedChannelBuilder.java#L31-L34).

----------

[<- Back to index](index)

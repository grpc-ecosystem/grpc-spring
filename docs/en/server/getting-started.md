# Getting Started

[<- Back to Index](../index.md)

This section describes the steps necessary to convert your application into a grpc-spring-boot-starter one.

## Table of Contents <!-- omit in toc -->

- [Project Setup](#project-setup)
- [Dependencies](#dependencies)
  - [Interface-Project](#interface-project)
  - [Server-Project](#server-project)
  - [Client-Project](#client-project)
- [Creating the gRPC-Service Definitions](#creating-the-grpc-service-definitions)
- [Implementing the Service](#implementing-the-service)

## Additional Topics <!-- omit in toc -->

- *Getting started*
- [Configuration](configuration.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Security](security.md)

## Project Setup

Before we start adding the dependencies lets start with some of our recommendation for your project setup.

![project setup](/grpc-spring-boot-starter/assets/images/server-project-setup.svg)

We recommend splitting your project into 2-3 separate modules.

1. **The interface project**
  Contains the raw protobuf files and generates the java model and service classes. You probably share this part.
2. **The server project**
  Contains the actual implementation of your project and uses the interface project as dependency.
3. **The client projects** (optional and possibly many)
  Any client projects that use the pre-generated stubs to access the server.

## Dependencies

### Interface-Project

#### Maven (Interface)

````xml
    <properties>
        <protobuf.version>3.14.0</protobuf.version>
        <protobuf-plugin.version>0.6.1</protobuf-plugin.version>
        <grpc.version>1.35.0</grpc.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <!-- Java 9+ compatibility -->
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
            <version>2.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.2</version>
            </extension>
        </extensions>

        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>${protobuf-plugin.version}</version>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
````

#### Gradle (Interface)

````gradle
buildscript {
    ext {
        protobufVersion = '3.14.0'
        protobufPluginVersion = '0.8.14'
        grpcVersion = '1.35.0'
    }
}

plugins {
    id 'java-library'
    id 'com.google.protobuf' version "${protobufPluginVersion}"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "io.grpc:grpc-protobuf:${grpcVersion}"
    implementation "io.grpc:grpc-stub:${grpcVersion}"
    implementation 'jakarta.annotation:jakarta.annotation-api:2.0.0' // Java 9+ compatibility
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    generatedFilesBaseDir = "$projectDir/src/generated"
    clean {
        delete generatedFilesBaseDir
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}

// Optional
eclipse {
    classpath {
        file.beforeMerged { cp ->
            def generatedGrpcFolder = new org.gradle.plugins.ide.eclipse.model.SourceFolder('src/generated/main/grpc', null);
            generatedGrpcFolder.entryAttributes['ignore_optional_problems'] = 'true';
            cp.entries.add( generatedGrpcFolder );
            def generatedJavaFolder = new org.gradle.plugins.ide.eclipse.model.SourceFolder('src/generated/main/java', null);
            generatedJavaFolder.entryAttributes['ignore_optional_problems'] = 'true';
            cp.entries.add( generatedJavaFolder );
        }
    }
}

// Optional
idea {
    module {
        sourceDirs += file("src/generated/main/java")
        sourceDirs += file("src/generated/main/grpc")
        generatedSourceDirs += file("src/generated/main/java")
        generatedSourceDirs += file("src/generated/main/grpc")
    }
}
````

### Server-Project

#### Maven (Server)

````xml
    <dependencies>
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-server-spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>example</groupId>
            <artifactId>my-grpc-interface</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
````

#### Gradle (Server)

````gradle
apply plugin: 'org.springframework.boot'

dependencies {
    compile('org.springframework.boot:spring-boot-starter')
    compile('net.devh:grpc-server-spring-boot-starter')
    compile('my-example:my-grpc-interface')
}

buildscript {
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
}

````

### Client-Project

See the [client getting started page](../client/getting-started.md#client-project)

## Creating the gRPC-Service Definitions

Place your protobuf definitions / `.proto` files in `src/main/proto`.
For writing protobuf files please refer to the official
[protobuf docs](https://developers.google.com/protocol-buffers/docs/proto3).

Your `.proto` files will look similar to the example below:

````proto
syntax = "proto3";

package net.devh.boot.grpc.example;

option java_multiple_files = true;
option java_package = "net.devh.boot.grpc.examples.lib";
option java_outer_classname = "HelloWorldProto";

// The greeting service definition.
service MyService {
    // Sends a greeting
    rpc SayHello (HelloRequest) returns (HelloReply) {
    }
}

// The request message containing the user's name.
message HelloRequest {
    string name = 1;
}

// The response message containing the greetings
message HelloReply {
    string message = 1;
}
````

The configured maven/gradle protobuf plugins will then use invoke the
[`protoc`](https://mvnrepository.com/artifact/com.google.protobuf/protoc) compiler with the
[`protoc-gen-grpc-java`](https://mvnrepository.com/artifact/io.grpc/protoc-gen-grpc-java) plugin and generate the data
classes, grpc service `ImplBase`s and `Stub`s. Please note that other plugins such as
[reactive-grpc](https://github.com/salesforce/reactive-grpc) might generate additional/alternative classes that you have
to use instead. However, they can be used in a similar fashion.

- The `ImplBase` classes contain the base logic that map the dummy implementation to the grpc service methods.
  More about this in the [Implementing the service](#implementing-the-service) topic.
- The `Stub` classes are complete client implementations.
  More about this on the [Getting the client started](../client/getting-started.md) page.

## Implementing the Service

The `protoc-gen-grpc-java` plugin generates a class for each of your grpc services.
For example: `MyServiceGrpc` where `MyService` is the name of the grpc service in the proto file. This class
contains both the client stubs and the server `ImplBase` that you will need to extend.

After that you have only four tasks to do:

1. Make sure that your `MyServiceImpl` extends `MyServiceGrpc.MyServiceImplBase`
2. Add the `@GrpcService` annotation to your `MyServiceImpl` class
3. Make sure that the `MyServiceImpl` is added to your application context,
   - either by creating `@Bean` definition in one of your `@Configuration` classes
   - or placing it in spring's automatically detected paths (e.g. in the same or a sub package of your `Main` class)
4. Actually implement the grpc service methods.

Your grpc service class will then look somewhat similar to the example below:

````java
import example.HelloReply;
import example.HelloRequest;
import example.MyServiceGrpc;

import io.grpc.stub.StreamObserver;

import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class MyServiceImpl extends MyServiceGrpc.MyServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
````

> **Note**: Theoretically it is not necessary to extend the `ImplBase` and instead implement `BindableService` yourself.
> However, doing so might result in bypassing spring security's checks.

That's all there is to that. Now you can start your spring-boot application and start sending requests to your
grpc-service.

By default, the grpc-server will be started on port `9090` using `PLAINTEXT` mode.

You can test that your application is working as expected by running these [gRPCurl](https://github.com/fullstorydev/grpcurl) commands:

````sh
grpcurl --plaintext localhost:9090 list
grpcurl --plaintext localhost:9090 list net.devh.boot.grpc.example.MyService
# Linux (Static content)
grpcurl --plaintext -d '{"name": "test"}' localhost:9090 net.devh.boot.grpc.example.MyService/sayHello
# Windows or Linux (dynamic content)
grpcurl --plaintext -d "{\"name\": \"test\"}" localhost:9090 net.devh.boot.grpc.example.MyService/sayHello
````

See [here](testing.md#grpcurl) for `gRPCurl` example command output and additional information.

> Note: Don't forget to write [actual/automated tests](testing.md) for your service implementation.

## Additional Topics <!-- omit in toc -->

- *Getting Started*
- [Configuration](configuration.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- [Testing the Service](testing.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

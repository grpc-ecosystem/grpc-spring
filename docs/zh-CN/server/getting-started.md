# 入门指南

[<- 返回索引](../index.md)

本节描述了将您的应用程序接入 grpc-spring-boot-starter 项目的必要步骤。

## 目录 <!-- omit in toc -->

- [项目创建](#项目创建)
- [依赖项](#依赖项)
  - [接口项目](#接口项目)
  - [服务端项目](#服务端项目)
  - [客户端项目](#客户端项目)
- [创建 gRPC 服务定义](#创建 gRPC 服务定义)
- [实现服务逻辑](#实现服务逻辑)

## 附加主题 <!-- omit in toc -->

- *入门指南*
- [配置](configuration.md)
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [服务端事件](events.md)
- [安全性](security.md)

## 项目创建

在我们开始添加依赖关系之前，让我们项目的一些设置建议开始。

![项目创建](/grpc-spring/assets/images/server-project-setup.svg)

我们建议将您的项目分为2至3个不同的模块。

1. **interface 项目** 包含原始 protobuf 文件并生成 java model 和 service 类。 你可能会在不同的项目中会共享这个部分。
2. **Server 项目** 包含项目的业务实现，并使用上面的 Interface 项目作为依赖项。
3. **Client 项目**（可选，可能很多） 任何使用预生成的 stub 来访问服务器的客户端项目。

## 依赖项

### 接口项目

#### Maven (Interface)

````xml
    <properties>
        <protobuf.version>3.23.4</protobuf.version>
        <protobuf-plugin.version>0.6.1</protobuf-plugin.version>
        <grpc.version>1.58.0</grpc.version>
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
            <!-- Java 9+ compatibility - Do NOT update to 2.0.0 -->
            <groupId>jakarta.annotation</groupId>
            <artifactId>jakarta.annotation-api</artifactId>
            <version>1.3.5</version>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.7.0</version>
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
        protobufVersion = '3.23.4'
        protobufPluginVersion = '0.8.18'
        grpcVersion = '1.58.0'
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
    compileOnly 'jakarta.annotation:jakarta.annotation-api:1.3.5' // Java 9+ compatibility - Do NOT update to 2.0.0
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

### 服务端项目

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

### 客户端项目

请参阅 [客户端入门指引](../client/getting-started.md#client-project) 页面

## 创建 gRPC 服务定义

将您的 protobuf 定义/`.proto`文件放入`src/main/proto`。 有关编写 protobuf 文件的信息，请参阅官方的 [protobuf 文档](https://developers.google.com/protocol-buffers/docs/proto3)。

您的 `.proto` 文件跟如下的示例类似：

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

配置 maven/gradle protobuf 插件使其调用 [`protoc`](https://mvnrepository.com/artifact/com.google.protobuf/protoc) 编译器，并使用 [`protoc-gen-grpc-java`](https://mvnrepository.com/artifact/io.grpc/protoc-gen-grpc-java) 插件并生成数据类、grpc 服务类 `ImplicBase`s 和 `Stub`。 请注意，其他插件，如 [reactive-grpc](https://github.com/salesforce/reactive-grpc) 可能会生成其他额外 / 替代类。 然而，它们也可以同样的方式使用。

- `ImplicBase`类包含基本逻辑，映射虚拟实现到grpc 服务方法。 在 [实现服务逻辑](#implementing-the-service) 章节中有更多关于这个问题的信息。
- `Stub`类是完整的客户端实现。 更多信息可以参考 [客户端指引](../client/getting-started.md) 页面。

## 实现服务逻辑

`protoc-gen-grpc-java` 插件为你的每个 grpc 服务生成一个类。 例如：`MyServiceGrpc` 的 `MyService` 是 proto 文件中的 grpc 服务名称。 这个类 包含您需要扩展的客户端 stub 和服务端的 `ImplicBase`。

在这之后，你还有四个步骤：

1. 请确保您的 `MyServiceImp` 实现了 `MyServiceGrpc.MyServiceImpBase`
2. 将 `@GrpcService` 注解添加到您的 `MyServiceImp` 类上
3. 请确保 `MyServiceImplic` 已添加到您的应用程序上下文中。
   - 通过在您的 `@Configuration` 类中创建 `@Bean`
   - 或者将其放置在 spring 的自动检测到路径中(例如在您`Main`类的相同或子包中)
4. 实现 grpc 服务方法。

您的 grpc 服务类将会看起来与下面的例子有些相似：

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

> **注意**: 理论上来说，不需要拓展 `ImplBase` 而是自己实现  `BindableService`。 但是，这样做可能会导致绕过 Spring Security 的检查。

上面就是你接入过程中所有需要做的。 现在您可以启动您的 spring-boot 应用程序并开始向您的 grpc 服务发送请求。

默认情况下，grpc-server 将使用 `PLAINTEXT` 模式在端口 `9090` 中启动。

您可以通过运行 [gRPCurl](https://github.com/fullstorydev/grpcurl) 命令来测试您的应用程序是否正常运行：

````sh
grpcurl --plaintext localhost:9090 list
grpcurl --plaintext localhost:9090 list net.devh.boot.grpc.example.MyService
# Linux (Static content)
grpcurl --plaintext -d '{"name": "test"}' localhost:9090 net.devh.boot.grpc.example.MyService/sayHello
# Windows or Linux (dynamic content)
grpcurl --plaintext -d "{\"name\": \"test\"}" localhost:9090 net.devh.boot.grpc.example.MyService/sayHello
````

`gRPCurl` 的示例命令输出以及其他信息请参考此 [文档](testing.md#grpcurl) 。

> 注意：不要忘记为您的服务实现编写 [实际/自动测试](testing.md)。

## 附加主题 <!-- omit in toc -->

- *入门指南*
- [配置](configuration.md)
- [异常处理](exception-handling.md)
- [上下文数据 / Bean 的作用域](contextual-data.md)
- [测试服务](testing.md)
- [服务端事件](events.md)
- [安全性](security.md)

----------

[<- 返回索引](../index.md)

# 测试服务

[<- Back to Index](../index.md)

本节介绍如何为您的 grpc-service 编写测试用例。

If you want to test a component that internally uses an `@GrpcClient` annotated field or one of grpc's stubs. Please refer to [Tests with Grpc-Stubs](../client/testing.md).

## 目录 <!-- omit in toc -->

- [前言](#introductory-words)
- [测试服务](#the-service-to-test)
- [有用的依赖项](#useful-dependencies)
- [单元测试](#unit-tests)
  - [独立测试](#standalone-tests)
  - [基于Spring的测试](#spring-based-tests)
- [集成测试](#integration-tests)
- [gRPCurl](#grpcurl)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- *Testing the Service*
- [Server Events](events.md)
- [Security](security.md)

## 前言

我们都知道测试对我们的应用程序是多么重要，所以我只会在这里向大家介绍几个链接：

- [Testing Spring](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html)
- [Testing with JUnit](https://junit.org/junit5/docs/current/user-guide/#writing-tests)
- [grpc-spring-boot-starter's Tests](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/tests/src/test/java/net/devh/boot/grpc/test)

Generally there are three ways to test your grpc service:

- [直接测试](#unit-tests)
- [通过 grpc 测试](#integration-tests)
- [Test them in production](#grpcurl) (in addition to automated build time tests)

## 测试服务

让我们假设，我们希望测试以下服务：

````java
@GrpcService
public class MyServiceImpl extends MyServiceGrpc.MyServiceImplBase {

    private OtherDependency foobar;

    @Autowired
    public void setFoobar(OtherDependency foobar) {
        this.foobar = foobar;
    }

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply response = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .setCounter(foobar.getCount())
                .build();
        responseObserver.onNext(response);
        responseObserver.onComplete();
    }

}
````

## 有用的依赖项

在您开始编写自己的测试框架之前，您可能想要使用以下库来使您的工作更加简单。

> **Note:** Spring-Boot-Test already contains some of these dependencies, so make sure you exclude conflicting versions.

对于Maven来说，添加以下依赖：

````xml
<!-- JUnit-Test-Framework -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <scope>test</scope>
</dependency>
<!-- Grpc-Test-Support -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-testing</artifactId>
    <scope>test</scope>
</dependency>
<!-- Spring-Test-Support (Optional) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <!-- Exclude the test engine you don't need -->
    <exclusions>
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>
````

Gradle 使用：

````groovy
// JUnit-Test-Framework
testImplementation("org.junit.jupiter:junit-jupiter-api")
testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
// Grpc-Test-Support
testImplementation("io.grpc:grpc-testing")
// Spring-Test-Support (Optional)
testImplementation("org.springframework.boot:spring-boot-starter-test") {
    // Exclude the test engine you don't need
    exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
}
````

## 单元测试

在直接测试中，我们直接在 grpc-service bean/实例上调用方法。

> 如果您自己创建的 grpc-service 实例, 请确保您先处理所需的依赖关系。 如果您使用Spring，它会处理您的依赖关系，但作为代价，您必须配置Spring。

### 独立测试

独立测试对外部库没有任何依赖关系(事实上你甚至不需要这个项目)。 然而，没有外部依赖关系并不总能使您的生活更加容易， 您可能需要复制其他库来执行您的行为。 使用 [Mockito](https://site.mockito.org) 这样的模拟库会简化你的流程，因为它限制依赖树的深度。

````java
public class MyServiceTest {

    private MyServiceImpl myService;

    @BeforeEach
    public void setup() {
        myService = new MyServiceImpl();
        OtherDependency foobar = ...; // mock(OtherDependency.class)
        myService.setFoobar(foobar);
    }

    @Test
    void testSayHellpo() throws Exception {
        HelloRequest request = HelloRequest.newBuilder()
                .setName("Test")
                .build();
        StreamRecorder<HelloReply> responseObserver = StreamRecorder.create();
        myService.sayHello(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        List<HelloReply> results = responseObserver.getValues();
        assertEquals(1, results.size());
        HelloReply response = results.get(0);
        assertEquals(HelloReply.newBuilder()
                .setMessage("Hello ==> Test")
                .setCounter(1337)
                .build(), response);
    }

}
````

### 基于Spring的测试

如果您使用Spring来管理您自己的依赖关系，您实际上正在进入集成测试领域。 请确保您不要启动整个应用程序，而只提供所需的依赖关系为 (模拟) 的 Bean 类。

> **注意:** 在测试期间，Spring 不会自动配置所有必须的 Bean。 您必须在有`@Configuration` 注解的类中手动创建它们。

````java
@SpringBootTest
@SpringJUnitConfig(classes = { MyServiceUnitTestConfiguration.class })
// Spring doesn't start without a config (might be empty)
// Don't use @EnableAutoConfiguration in this scenario
public class MyServiceTest {

    @Autowired
    private MyServiceImpl myService;

    @Test
    void testSayHellpo() throws Exception {
        HelloRequest request = HelloRequest.newBuilder()
                .setName("Test")
                .build();
        StreamRecorder<HelloReply> responseObserver = StreamRecorder.create();
        myService.sayHello(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        List<HelloReply> results = responseObserver.getValues();
        assertEquals(1, results.size());
        HelloReply response = results.get(0);
        assertEquals(HelloReply.newBuilder()
                .setMessage("Hello ==> Test")
                .setCounter(1337)
                .build(), response);
    }

}
````

和所需的配置类：

````java
@Configuration
public class MyServiceUnitTestConfiguration {

    @Bean
    OtherDependency foobar() {
        // return mock(OtherDependency.class);
    }

    @Bean
    MyServiceImpl myService() {
        return new MyServiceImpl();
    }

}
````

## 集成测试

然而，您有时需要测试整个调用栈。 例如，如果认证发挥了作用。 但在这种情况下，建议限制您的测试范围，以避免像 空数据库这样可能的外部影响。

在这一点上，不使用 Spring 测试您的 Spring 应用程序是毫无意义的。

> **注意:** 在测试期间，Spring 不会自动配置所有必须的 Bean。 您必须在有 `@Configuration` 注解修饰的类中手动创建他们，或显式的包含相关的自动配置类。

````java
@SpringBootTest(properties = {
        "grpc.server.inProcessName=test", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.inProcess.address=in-process:test" // Configure the client to connect to the inProcess server
        })
@SpringJUnitConfig(classes = { MyServiceIntegrationTestConfiguration.class })
// Spring doesn't start without a config (might be empty)
@DirtiesContext // Ensures that the grpc-server is properly shutdown after each test
        // Avoids "port already in use" during tests
public class MyServiceTest {

    @GrpcClient("inProcess")
    private MyServiceBlockingStub myService;

    @Test
    @DirtiesContext
    public void testSayHello() {
        HelloRequest request = HelloRequest.newBuilder()
                .setName("test")
                .build();
        HelloReply response = myService.sayHello(request);
        assertNotNull(response);
        assertEquals("Hello ==> Test", response.getMessage())
    }

}
````

所需的配置看起来像这样：

````java
@Configuration
@ImportAutoConfiguration({
        GrpcServerAutoConfiguration.class, // Create required server beans
        GrpcServerFactoryAutoConfiguration.class, // Select server implementation
        GrpcClientAutoConfiguration.class}) // Support @GrpcClient annotation
public class MyServiceIntegrationTestConfiguration {

    @Bean
    OtherDependency foobar() {
        return ...; // mock(OtherDependency.class);
    }

    @Bean
    MyServiceImpl myServiceImpl() {
        return new MyServiceImpl();
    }

}
````

> 注意：这个代码看起来可能比单元测试更短/更简单，但执行时间要长一些。

## gRPCurl

[`gRPCurl`](https://github.com/fullstorydev/grpcurl) is a small command line application, that you can use to query your application at runtime. Or as their Readme states:

> It's basically `curl` for gRPC servers.

You can even use the responses with `jq` and use it in your automation.

Skip the first/this block if you already know what you wish to query.

````bash
$ # First scan the server for available services
$ grpcurl --plaintext localhost:9090 list
net.devh.boot.grpc.example.MyService
$ # Then list the methods available for that call
$ grpcurl --plaintext localhost:9090 list net.devh.boot.grpc.example.MyService
net.devh.boot.grpc.example.MyService.SayHello
$ # Lets check the request and response types
$ grpcurl --plaintext localhost:9090 describe net.devh.boot.grpc.example.MyService/SayHello
net.devh.boot.grpc.example.MyService.SayHello is a method:
rpc SayHello ( .HelloRequest ) returns ( .HelloReply );
$ # Now we only have query for the request body structure
$ grpcurl --plaintext localhost:9090 describe net.devh.boot.grpc.example.HelloRequest
net.devh.boot.grpc.example.HelloRequest is a message:
message HelloRequest {
  string name = 1;
}
````

> Note: `gRPCurl` supports both `.` and `/` as separator between the service name and the method name:
> 
> - `net.devh.boot.grpc.example.MyService.SayHello`
> - `net.devh.boot.grpc.example.MyService/SayHello`
> 
> We recommend the second variant as it matches grpc's internal full method name and the method name is easier to detect in the call.

````bash
$ # Finally we can call the actual method
$ grpcurl --plaintext localhost:9090 net.devh.boot.grpc.example.MyService/SayHello
{
  "message": "Hello ==> ",
  "counter": 1337
}
$ # Or call it with a populated request body
$ grpcurl --plaintext -d '{"name": "Test"}' localhost:9090 net.devh.boot.grpc.example.MyService/SayHello
{
  "message": "Hello ==> Test",
  "counter": 1337
}
````

> Note: If you use the windows terminal or wish to use variables inside the data block then you have to use `"` instead of `'` and escape the `"` that are part of the actual json.
> 
> ````cmd
> 
> > grpcurl --plaintext -d "{\"name\": \"Test\"}" localhost:9090 net.devh.boot.grpc.example.MyService/sayHello
    {
      "message": "Hello ==> Test",
      "counter": 1337
    }
    ````

For more information regarding `gRPCurl` please refer to their [official documentation](https://github.com/fullstorydev/grpcurl)

## Additional Topics <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [Exception Handling](exception-handling.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- *Testing the Service*
- [Server Events](events.md)
- [Security](security.md)

----------

[<- Back to Index](../index.md)

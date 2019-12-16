# 测试服务

[<- index返回索引](../index)

本节介绍如何为您的 grpc-service 编写测试用例。

## 目录 <!-- omit in toc -->

- [前言](#introductory-words)
- [测试服务](#the-service-to-test)
- [有用的依赖项](#useful-dependencies)
- [单元测试](#unit-tests)
  - [独立测试](#standalone-tests)
  - [基于Spring的测试](#spring-based-tests)
- [集成测试](#integration-tests)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started)
- [配置](configuration)
- [上下文数据 / Bean 的作用域](contextual-data)
- *测试服务*
- [安全性](security)

## 前言

我们都知道测试对我们的应用程序是多么重要，所以我只会在这里向大家介绍几个链接：

- [Testing Spring](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html)
- [Testing with JUnit](https://junit.org/junit5/docs/current/user-guide/#writing-tests)
- [grpc-spring-boot-starter's Tests](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/tests/src/test/java/net/devh/boot/grpc/test)

通常有两种方法来测试您的 grpc 服务：

- [直接测试](#unit-tests)
- [通过 grpc 测试](#integration-tests)

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

对于Maven来说，添加以下依赖：

````xml
<！-- JUnit-elotelFramework -->
<dependency>
    <groupId>org.junit。 upiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org。 unit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <scope>test</scope>
</dependency>
<- Grpc-extract Support -->
<dependency>
    <groupId>io. rpc</groupId>
    <artifactId>grpc-testing</artifactId>
    <scope>test</scope>
</dependency>
<！ - Spring-Extract Support (Optional) -->
<dependency>
    <groupId>org。 pringframework.boot</groupId>
    <artifactId>spring-boot-start-test</artifactId>
    <scope>test</scope>
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
testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    void testSayHellpo() {
        HelloRequest request = HelloRequest.newBuilder()
                .setName("Test")
                .build();
        StreamRecorder<HelloReply> responseObserver = StreamRecorder.create();
        myService.sayHello(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS);
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        List<HelloReply> results = responseObserver.getValues().size();
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
    void testSayHellpo() {
        HelloRequest request = HelloRequest.newBuilder()
                .setName("Test")
                .build();
        StreamRecorder<HelloReply> responseObserver = StreamRecorder.create();
        myService.sayHello(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS);
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        List<HelloReply> results = responseObserver.getValues().size();
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

## 附加主题<!-- omit in toc -->- [入门指南](getting-started)
- [配置](configuration)
- [上下文数据 / Bean 的作用域](contextual-data)
- *测试服务*
- [安全性](security)

----------

[<- index返回索引](../index)

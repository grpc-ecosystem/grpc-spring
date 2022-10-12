# 使用 Grpc-Stubs 测试

[<- 返回索引](../index.md)

本节描述如何为使用了 `@GrpcClient` 注解或 grpc stub 的组件编写测试用例。

## 目录 <!-- omit in toc -->

- [前言](#前言)
- [要测试的组件](#要测试的组件)
- [有用的依赖项](#有用的依赖项)
- [使用 Mocked Stub](#使用 Mocked Stub)
- [运行一个虚拟服务](#运行一个虚拟服务)

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [安全性](security.md)
- *使用 Grpc-Stubs 测试*

## 前言

通常有两种方法来测试你包含 grpc stub 的组件：

- [使用 Mocked Stub](#使用 Mocked Stub)
- [运行一个虚拟服务](#运行一个虚拟服务)

> 注意：在测试期间，这两种形式有着非常明显的差异。 请仔细考虑每种形式中列出的利弊。

## 要测试的组件

让我们假设，我们希望测试以下组件：

````java
@Component
public class MyComponent {

    private ChatServiceBlockingStub chatService;

    @GrpcClient("chatService")
    public void setChatService(ChatServiceBlockingStub chatService) {
        this.chatService = chatService;
    }

    public String sayHello(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        HelloReply reply = chatService.sayHello(name)
        return reply.getMessage();
    }

}
````

## 有用的依赖项

在您开始编写自己的测试框架之前，您可能想要使用以下库来使您的工作更加简单。

> **注意：** Spring-Boot-Test已经包含一些依赖项，所以请确保您排除掉了冲突的版本。

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
<!-- Mocking Framework (Optional) -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-all</artifactId>
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
testImplementation("org.springframework.boot:spring-boot-starter-test") {
    // Exclude the test engine you don't need
    exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
}
// Mocking Framework (Optional)
testImplementation("org.mockito:mockito-all")
````

## 使用 Mocked Stub

为了测试该方法，我们模拟 stub 并用 setter 注入它。

### 优点

- 快
- 支持主流的 mocking 框架

### 缺点

- 需要“魔法”才能作用在 un-final 修饰的 stub 中
- 无法开箱即用
- 无法在含有 `@PostContrast` 注解且方法中有使用到 stub 的 bean 中工作
- 间接使用 stub （通过其他 bean）的 bean 可能无法正常工作
- 在有启动 Spring 的测试用例中可能无法正常工作

### 实现

1. 将 mockito 添加到 dependencies 中(见[ 上文 ](#useful-dependencies))
2. 配置 mockito 使它与 final 修饰的类/方法一起工作

   为此，我们需要创建一个文件 `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` 包含：

   ````txt
   mock-maker-inline
   ````

3. 像往常一样编写我们的 stub，并在测试过程中明确地将它设置到你的组件里

   ````java
   public class MyComponentTest {

       private MyComponent myComponent = new MyComponent();
       private ChatServiceBlockingStub chatService = Mockito.mock(ChatServiceBlockingStub.class);

       @BeforeEach
       void setup() {
           myComponent.setChatService(chatService);
       }

       @Test
       void testSayHello() {
           Mockito.when(chatService.sayHello(...)).thenAnswer(...);
           assertThat(myComponent.sayHello("ThisIsMyName")).contains("ThisIsMyName");
       }

   }
   ````

## 运行一个虚拟服务

为了测试这个方法，我们自己启动了一个 grpc 服务端，并在测试中连接到它。

### 优点

- 无需伪造与组件有关的任何信息
- 没有“魔法”

### 缺点

- 要求我们伪造实现实际的服务
- 需要 Spring 才能运行

### 实现

实际的使用方式像下面这样：

````java
@SpringBootTest(properties = {
        "grpc.server.inProcessName=test", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.chatService.address=in-process:test" // Configure the client to connect to the inProcess server
        })
@SpringJUnitConfig(classes = { MyComponentIntegrationTestConfiguration.class })
// Spring doesn't start without a config (might be empty)
@DirtiesContext // Ensures that the grpc-server is properly shutdown after each test
        // Avoids "port already in use" during tests
public class MyComponentTest {

    @Autowired
    private MyComponent myComponent;

    @Test
    @DirtiesContext
    void testSayHello() {
        assertThat(myComponent.sayHello("ThisIsMyName")).contains("ThisIsMyName");
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
public class MyComponentIntegrationTestConfiguration {

    @Bean
    MyComponent myComponent() {
        return new MyComponent();
    }

    @Bean
    ChatServiceImplForMyComponentIntegrationTest chatServiceImpl() {
        return new ChatServiceImplForMyComponentIntegrationTest();
    }

}
````

虚拟的服务可能看起来像这样:

````java
@GrpcService
public class ChatServiceImplForMyComponentIntegrationTest extends ChatServiceGrpc.ChatServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply response = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onComplete();
    }

    // Methods that aren't used in the test don't need to be implemented

}
````

## 附加主题 <!-- omit in toc -->

- [入门指南](getting-started.md)
- [配置](configuration.md)
- [安全性](security.md)
- *使用 Grpc-Stubs 测试*

----------

[<- 返回索引](../index.md)

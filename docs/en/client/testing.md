# Tests with Grpc-Stubs

[<- Back to Index](../index.md)

This section describes how you write tests for components that use the `@GrpcClient` annotation or grpc's stubs.

## Table of Contents <!-- omit in toc -->

- [Introductory Words](#introductory-words)
- [The Component to test](#the-component-to-test)
- [Useful Dependencies](#useful-dependencies)
- [Using a Mocked Stub](#using-a-mocked-stub)
- [Running a Dummy Server](#running-a-dummy-server)
- [Skipping injection](#skipping-injection)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Security](security.md)
- *Tests with Grpc-Stubs*

## Introductory Words

Generally there are two ways to test your component containing a grpc stub:

- [Using a Mocked Stub](#using-a-mocked-stub)
- [Running a Dummy Server](#running-a-dummy-server)

> Note: There are very important differences in both variants that might affect you during the tests.
> Please consider the pros and cons listed at each on the variants carefully.

## The Component to test

Let's assume that we wish to test the following component:

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

## Useful Dependencies

Before you start writing your own test framework, you might want to use the following libraries to make your work easier.

> **Note:** Spring-Boot-Test already contains some of these dependencies, so make sure you exclude conflicting versions.

For Maven add the following dependencies:

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

For Gradle use:

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

## Using a Mocked Stub

In order to test the method we mock the stub and inject it using a setter.

### Pros

- Fast
- Supports well-known mocking frameworks

### Cons

- Requires "magic" to un-final the stub methods
- Doesn't work out of the box
- Doesn't work for beans that use the stub in `@PostContruct`
- Doesn't work well for beans that use the stub indirectly (via other beans)
- Doesn't work well for tests that start Spring

### Implementation

1. Add mockito to our dependencies (see [above](#useful-dependencies))
2. Configure mockito to work with final classes/methods

   For this we need to create a file `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` containing:

   ````txt
   mock-maker-inline
   ````

3. Write our mocks as usual and explicitly set it on your component in test

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

## Running a Dummy Server

In order to test the method we start a grpc server ourselves and connect to it during our tests.

### Pros

- No need to fake anything related to the component
- No "magic"

### Cons

- Requires us to fake implement the actual service
- Requires Spring to run

### Implementation

The actual implementation of the test might look somewhat like this:

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

and the required configuration looks like this:

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

and the dummy service implementation might look like this:

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

## Skipping injection

If you don't need a specific `@GrpcClient` in a test, then you can configure it to be skipped using the `null` scheme.
(In that case it will be injected with `null`)

````java
@SpringBootTest(properties = {
        "grpc.client.test.address=null:/",
}, ...)
class MyTest {

    @GrpcClient("test")
    Channel channel;

    @Test()
    void test() {
        assertNull(channel);
    }

}
````

> **Note:** Due to configuration limitations you cannot use just `null` or `null:` as address,
> you have to specify a scheme specific part e.g.: `null:/` or `null:null`.

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Security](security.md)
- *Tests with Grpc-Stubs*

----------

[<- Back to Index](../index.md)

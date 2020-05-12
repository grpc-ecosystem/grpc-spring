# Testing the Service

[<- Back to Index](../index.md)

This section describes how you write tests for your grpc-service implementation.

If you want to test a component that internally uses an `@GrpcClient` annotated field or one of grpc's stubs.
Please refer to [Tests with Grpc-Stubs](../client/testing.md).

## Table of Contents <!-- omit in toc -->

- [Introductory Words](#introductory-words)
- [The Service to Test](#the-service-to-test)
- [Useful Dependencies](#useful-dependencies)
- [Unit Tests](#unit-tests)
  - [Standalone Tests](#standalone-tests)
  - [Spring-based Tests](#spring-based-tests)
- [Integration Tests](#integration-tests)

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- *Testing the Service*
- [Security](security.md)

## Introductory Words

We all know how important it is to test our application, so I will only refer you to a few links here:

- [Testing Spring](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html)
- [Testing with JUnit](https://junit.org/junit5/docs/current/user-guide/#writing-tests)
- [grpc-spring-boot-starter's Tests](https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/tests/src/test/java/net/devh/boot/grpc/test)

Generally there are two ways to test your grpc service:

- [Test them directly](#unit-tests)
- [Test them via grpc](#integration-tests)

## The Service to Test

Let's assume that we wish to test the following service:

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
````

## Unit Tests

In the direct tests, we invoke the methods directly on the grpc-service bean/instance.

> If you create the grpc-service instance yourself, make sure that you populate the required dependencies first.
> If you use Spring, it will take care of the dependencies for you, but in return you will have to configure Spring.

### Standalone Tests

The standalone tests don't have any dependencies to external libraries (in fact you don't even need this one).
However, having no external dependencies doesn't always make your life easier, because you might have to replicate
behavior that other libraries might do for you. Using a mocking library such as [Mockito](https://site.mockito.org)
will simplify the process for you, as it limits the depth of the dependency tree.

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

### Spring-based Tests

If you use Spring to manage dependencies for yourself, you're actually tapping into the field of integration tests.
Make sure you don't start the entire application, but only provide the required dependencies as (mocked) beans.

> **Note:** During tests spring does not automatically setup all required beans. You have to manually create them in
> your `@Configuration` classes.

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

and the required configuration class:

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

## Integration Tests

Sometimes, however, you need to test the entire stack. For example, if authentication plays a role.
But also in this case it is recommended to limit the scope of your test to avoid possible external influences like an
empty database.

At this point it doesn't make any sense to test your spring based application without spring.

> **Note:** During tests spring does not automatically setup all required beans. You have to manually create them in
> your `@Configuration` or explicitly include the related auto configuration classes.

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

and the required configuration looks like this:

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

> Note: This code might look shorter/simpler than the unit test one, but the execution time is serveral times longer.

## Additional Topics <!-- omit in toc -->

- [Getting Started](getting-started.md)
- [Configuration](configuration.md)
- [Contextual Data / Scoped Beans](contextual-data.md)
- *Testing the Service*
- [Security](security.md)

----------

[<- Back to Index](../index.md)

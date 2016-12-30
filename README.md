# Grpc Spring Boot Starter

## Features
Auto-configures and runs the embedded gRPC server with @GrpcService-enabled beans as part of spring-boot application.

Support Spring Cloud(Through the spring cloud automatic registration services to consul or eureka or fetch grpc server information)

Support Spring Sleuth to trace application

## Usage

### gRPC server
Add ``@EnableGrpcServer`` annotation to you configuration for enable grpc server

Annotate your server interface implementation(s) with ``@GrpcService``
````java
@GrpcService(GreeterGrpc.class)
public class GrpcServerService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello =============> " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
````
### gRPC client
Add ``@EnableGrpcClient`` annotation to you configuration for enable grpc client

Use ``@GrpcClient`` annotation to set Channel
 
````java
@GrpcClient("grpc server name")
private Channel serverChannel;
````

grpc request

````java
GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverChannel);
HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName(name).build());
````

## Show case
https://github.com/yidongnan/grpc-spring-boot-starter/tree/master/example

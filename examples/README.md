# gRPC spring boot starter Examples

## Local Mode
1. Try the local-grpc-server example first run:

    ````
    ./gradlew :example:local-grpc-server:bootRun
    ````

2. In a different terminal window run:

    ````
    ./gradlew :example:local-grpc-client:bootRun
    ````

visit http://localhost:8080/ can see result.

## Cloud Mode

1. Try the cloud-eureka-server example first run:

    ````
    ./gradlew :example:cloud-eureka-server:bootRun
    ````

2. run zipkin-server

    https://github.com/openzipkin/zipkin#quick-start

3. In a different terminal window run:

    ````
    ./gradlew :example:cloud-grpc-server:bootRun
    ````

4. In a different terminal window run:

    ````
    ./gradlew :example:cloud-grpc-client:bootRun
    ````

visit http://localhost:8080/ can see result.
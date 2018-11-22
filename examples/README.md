# gRPC spring boot starter Examples

## Local Mode

1. Try the local-grpc-server example first run:

    ````sh
    ./gradlew :example:local-grpc-server:bootRun
    ````

2. In a different terminal window run:

    ````sh
    ./gradlew :example:local-grpc-client:bootRun
    ````

3. Visit http://localhost:8080/ to see the result.

## Cloud Mode

1. Try the cloud-eureka-server example first run:

    ````sh
    ./gradlew :example:cloud-eureka-server:bootRun
    ````

2. Run zipkin-server

    https://github.com/openzipkin/zipkin#quick-start

3. In a different terminal window run:

    ````sh
    ./gradlew :example:cloud-grpc-server:bootRun
    ````

4. In a different terminal window run:

    ````sh
    ./gradlew :example:cloud-grpc-client:bootRun
    ````

5. Visit http://localhost:8080/ to see the result.

## With security

1. Try the security-grpc-server example first run:

    ````sh
    ./gradlew :example:security-grpc-server:bootRun
    ````

2. In a different terminal window run:

    ````sh
    ./gradlew :example:security-grpc-client:bootRun
    ````

3. Visit http://localhost:8080/ to see the result.

*You can configure the client's username in the application.yml.*

# gRPC spring boot starter Examples

## Local Setup

1. Try the local-grpc-server example first run:

    ````sh
    ./gradlew :example:local-grpc-server:bootRun
    ````

2. In a different terminal window run:

    ````sh
    ./gradlew :example:local-grpc-client:bootRun
    ````

3. Visit http://localhost:8080/ to see the result.

## Cloud Discovery Setup

1. Choose a cloud discovery implementation:

   - `consul`
   - `eureka`
   - `nacos`

    > **Note:** In your actual application you are free to choose any cloud discovery implementation,
    > but only the above and `zookeeper` will automatically register the server at the discovery service.
    > So you might have to write a few extra lines in your server application.
    > Generic registration support is planned for a future release.
    > No additional configuration is required for clients.

2. Start the discovery server (only the chosen one):

    ````sh
    # Consul
    docker run --name=consul -p 8500:8500 consul
    # Eureka
    ./gradlew :example:cloud-eureka-server:bootRun
    # Nacos
    docker run --env MODE=standalone --name nacos -d --rm -p 8848:8848 nacos/nacos-server
    ````

3. Insert the selected implementation and start the server application (in a new terminal window):

    ````sh
    ./gradlew -Pdiscovery=$discovery :example:cloud-grpc-server:bootRun
    ````

4. Insert the selected implementation and start the client application (in a new terminal window):

    ````sh
    ./gradlew -Pdiscovery=$discovery :example:cloud-grpc-client:bootRun
    ````

5. Visit http://localhost:8080/ to see the result.

## With Basic auth security

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

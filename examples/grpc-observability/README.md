# gRPC-Observability Example

## Features

* End to end examples to generate the gRPC metrics with Prometheus, including:
    * A frontend microservice (
      in [gRPC-Spring](https://github.com/grpc-ecosystem/grpc-spring)), which
      keeps calling the backend microservices.
    * A backend microservice (
      in [gRPC-Spring](https://github.com/grpc-ecosystem/grpc-spring)), which
      serves unary/client streaming/server streaming and bidi streaming example
      services.
    * Instructions to containerize them and deploy them in kubernetes
* Monitoring dashboards to support
  the [gRPC A66](https://github.com/grpc/proposal/blob/master/A66-otel-stats.md)
  spec.
* Deploy the Grafana and import
  the [gRPC A66](https://github.com/grpc/proposal/blob/master/A66-otel-stats.md)
  dashboard

## Build the end to end example

Under the root directory of your grpc-spring repo

```
./gradlew build
```

## Run the end to end example

Run the backend microservice locally

```
./gradlew examples:grpc-observability:backend:bootRun
```

Run the frontend microservice locally. Please note that the backend needs to be
started first to be ready to serve the client calls.

```
./gradlew examples:grpc-observability:frontend:bootRun
```

The backend microservice will

- listen on the TCP port 9091 for the gRPC calls from the frontend microservice.
- listen on the TCP port 8081 for the Prometheus scrape requests.

The frontend microservice will

- send the unary/client streaming/server streaming/bidi streaming calls to the
  backend microservices via TCP port 9091.
- listen on the TCP port 8080 for the Prometheus scrape requests.

## Containerize the frontend/backend microservices

Build the docker image for the backend microservice

```
docker build -t grpc-observability/grpc-spring-example-backend examples/grpc-observability/backend
```

Run the backend microservice with docker

```
docker run --network host grpc-observability/grpc-spring-example-backend
```

Build the docker image for the frontend microservice

```
docker build -t grpc-observability/grpc-spring-example-frontend examples/grpc-observability/frontend
```

Run the frontend microservice with docker

```
docker run --network host grpc-observability/grpc-spring-example-frontend
```

## Deploy the end to end example to kubernetes

To deploy the example to kubernetes, please upload the docker images to a
registry by yourself and modify the frontend.yaml/backend.yaml with your docker
image location, and then run following commands.

Deploy the backend microservice in kubernetes

```
kubectl apply -f ./examples/grpc-observability/backend/backend.yaml
```

Deploy the frontend microservice in kubernetes

```
kubectl apply -f ./examples/grpc-observability/frontend/frontend.yaml
```

## Set up the Prometheus to collect the gRPC metrics

Once the frontend/backend microservices are deployed (either locally or on
cloud), you may set up the Prometheus to start scraping the metrics from them.
Depends on where you run the frontend/backend microservices, you may need to
deploy the Prometheus to a proper location to be able to access them, such as,
the same cloud, etc.

If you
use [Google Managed Prometheus](https://cloud.google.com/stackdriver/docs/managed-prometheus),
you may need to configure the PodMonitoring resource to tell where are endpoints
to scrape the Prometheus metrics.

```
kubectl apply -f ./examples/grpc-observability/pod_monitoring.yaml
```

## Set up the Grafana dashboard

Once we have the gRPC metrics scraped by the Prometheus, we may set up a Grafana
server to visualize them.

- Set up a Grafana server, deploy it to a place where can connect the Prometheus
  server as its data source.
- Create a Grafana dashboard by importing the
  [examples/grpc-observability/grafana/prometheus/microservices-grpc-dashboard.json](http://github.com/grpc-ecosystem/grpc-spring/blob/master/examples/grpc-observability/grafana/prometheus/microservices-grpc-dashboard.json)
  file.

If you
use [Google Managed Prometheus](https://cloud.google.com/stackdriver/docs/managed-prometheus),
you need to follow
the [Grafana Query User Guide](https://cloud.google.com/stackdriver/docs/managed-prometheus/query)
to set up the Grafana server
and [data source syncer](https://github.com/GoogleCloudPlatform/prometheus-engine/tree/main/cmd/datasource-syncer).

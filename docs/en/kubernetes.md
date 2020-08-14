# Kubernetes Setup

The following section assumes that you have at least some knowledge about deploying an application to Kubernetes.
For more details refer to the [official documentation](https://kubernetes.io/docs/home/)

Kubernetes (or more precisely most of kubernetes DNS provider's) expose enough information for grpc-java to resolve
the addresses of services that run inside the cluter. Should also work for OKD/OpenShift.

There are a few things you should keep in mind here though.

1. Inside your (target's) deployment, make sure that you expose the port specified by `grpc.server.port`
   (defaults to `9090`)

````yaml
[...]
    spec:
      containers:
      - name: my-grpc-server-app
        image: ...
        ports:
        - name: grpc # Use whatever name you want
          containerPort: 9090 # Use the same as `grpc.server.port` (prefer 80, 443 or 9090)
[...]
````

> **Note:** Container ports can be re-used by other deployments/pods, unless you use `hostPort`s.
> So there is no reason not to use a default one.

2. Inside your (target's) service definition, you should map that port to your preferred port.

````yaml
apiVersion: v1
kind: Service
metadata:
  name: my-grpc-server-app # This name is important
  namespace: example # This name might be important
spec:
  selector:
    app: my-grpc-server-app
  ports:
    - name: grpclb # The name is important, if you wish to use DNS-SVC-Lookups (must be grpclb)
      port: 1234 # Remember this port number, unless you use DNS-SVC-Lookups (prefer 80, 443 or 9090)
      targetPort: grpc # Use the port name from the deployment (or just the port number)
````

> **Note:** Service ports can be re-used by other services, unless you use `hostPort`s.
> So there is no reason not to use a default one.

3. Inside your client application config, configure the channel address to refer to the service name:

````properties
## Choose your matching variant
# Same namespace (target port=80 or derived by DNS-SVC)
grpc.clients.my-grpc-server-app.address=dns:///my-grpc-server-app
# Same namespace (different port)
grpc.clients.my-grpc-server-app.address=dns:///my-grpc-server-app:1234
# Different namespace
grpc.clients.my-grpc-server-app.address=dns:///my-grpc-server-app.example:1234
# Different cluster
grpc.clients.my-grpc-server-app.address=dns:///my-grpc-server-app.example.svc.cluster.local:1234
# Format
grpc.clients.my-grpc-server-app.address=dns:///<serviceName>[.<namespace>[.<clusterAddress>]][:<service-port>]
````

> **Note:** DNS-SVC lookups require the `grpclb` dependency to be present and the service's port name to be `grpclb`.
> Refer to grpc's official docs for more details.

# Kubernetes 设置

下面的部分假设你至少有一些关于将应用程序部署到Kubernetes的知识。 更多详情，请参阅 [官方文档](https://kubernetes.io/docs/home/)

Kubernetes（或者更准确地说是大多数 kubernetes DNS 提供商）为 grpc-java 暴露了足够的信息来解析在集群内运行的服务的地址。 同样在 OKD/OpenShift 上也能正常工作。

不过，您应该记住几件事情。

1. 在您的（目标）deployment 中，确保您暴露 `grpc.server.port` 指定的端口 （默认为 `9090`）

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

> **注意：** 容器端口可以被其他部署/pods重新使用，除非您使用 `hostPort`。 因此，没有理由不使用默认的。

2. 在您的（目标） 服务定义中，您应该将该端口映射到您首选的端口。

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

> **注意：** Service 端口可以被其他服务重新使用，除非您使用 `hostPort`。 因此，没有理由不使用默认值。

3. 在您的客户端应用程序配置中，配置 channel 地址指向服务名称：

````properties
## Choose your matching variant
# Same namespace (target port=80 or derived by DNS-SVC)
grpc.client.my-grpc-server-app.address=dns:///my-grpc-server-app
# Same namespace (different port)
grpc.client.my-grpc-server-app.address=dns:///my-grpc-server-app:1234
# Different namespace
grpc.client.my-grpc-server-app.address=dns:///my-grpc-server-app.example:1234
# Different cluster
grpc.client.my-grpc-server-app.address=dns:///my-grpc-server-app.example.svc.cluster.local:1234
# Format
grpc.client.my-grpc-server-app.address=dns:///<serviceName>[.<namespace>[.<clusterAddress>]][:<service-port>]
````

> **注意：** DNS-SVC 查找存在 `grpclb` 依赖项 ，服务端口名称必须是 `grpclb` 详情请参阅gRPC的官方文档。

----------

[<- 返回索引](index.md)

# 基准测试

[<- 返回索引](index)

这个项目不会给 gRPC-java 增加任何性能开销。

请参考官方的 gRPC 基准测试：

- [grpc.io: Benchmarking](https://grpc.io/docs/guides/benchmarking/)
- [grpc-java: Running Benchmarks](https://github.com/grpc/grpc-java/tree/master/benchmarks#grpc-benchmarks)

与纯 HTTP 相比，gRPC 具有很多优势，但是很难将它数字化。 经过高度优化的 Web 服务器的性能与普通的 gRPC 服务器可能一样好。

下面是普通HTTP与 grpc 的主要优点/差异：

- 二进制数据格式 (更快, 但不可读)
- Protobuf 定义的数据结构，可以用于为许多语言生成数据类和客户端。
- HTTP/2 连接和连接池

----------

[<- 返回索引](index)

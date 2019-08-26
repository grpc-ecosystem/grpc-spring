# Benchmarking

[<- Back to index](index)

This library does not add any performance overhead to gRPC-java.

Please refer to the official gRPC benchmarks:

- [grpc.io: Benchmarking](https://grpc.io/docs/guides/benchmarking/)
- [grpc-java: Running Benchmarks](https://github.com/grpc/grpc-java/tree/master/benchmarks#grpc-benchmarks)

gRPC offers various benefits over plain HTTP, but it's hard to put it in actual numbers.
Heavily optimized web-servers perform as good as normal gRPC-servers.

Here are the main benefits/differences from grpc over plain HTTP:

- Binary data format (a lot faster, but not human readable)
- Protobuf defined data scheme, that can be used to generate data classes and clients for many languages
- HTTP/2 connections and connection pooling

----------

[<- Back to index](index)

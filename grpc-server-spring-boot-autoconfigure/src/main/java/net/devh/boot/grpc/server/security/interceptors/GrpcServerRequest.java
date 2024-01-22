package net.devh.boot.grpc.server.security.interceptors;

import brave.grpc.GrpcRequest;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerInterceptor;

/**
 * Allows access gRPC specific aspects of a server request during sampling and parsing.
 *
 * @see GrpcRequest for a parsing example
 * @since 5.12
 */
public class GrpcServerRequest {
  final ServerCall<?, ?> call;
  final Metadata headers;

  public GrpcServerRequest(ServerCall<?, ?> call, Metadata headers) {
    if (call == null) throw new NullPointerException("call == null");
    if (headers == null) throw new NullPointerException("headers == null");
    this.call = call;
    this.headers = headers;
  }

  /**
   * Returns the {@linkplain ServerCall server call} passed to {@link
   * ServerInterceptor#interceptCall}.
   *
   * @since 5.12
   */
  public ServerCall<?, ?> call() {
    return call;
  }

  /**
   * Returns {@linkplain ServerCall#getMethodDescriptor()}} from the {@link #call()}.
   *
   * @since 5.12
   */
  public MethodDescriptor<?, ?> methodDescriptor() {
    return call.getMethodDescriptor();
  }

  /**
   * Returns the {@linkplain Metadata headers} passed to {@link ServerInterceptor#interceptCall}.
   *
   * @since 5.12
   */
  public Metadata headers() {
    return headers;
  }
}

package net.devh.boot.grpc.server.security.interceptors;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerInterceptor;

import static java.util.Objects.requireNonNull;

/**
 * Allows access gRPC specific aspects of a server request during sampling and parsing.
 *
 * @author Sajad Mehrabi (mehrabisajad@gmail.com)
 */
public class GrpcServerRequest {
  private final ServerCall<?, ?> call;
  private final Metadata headers;

  public GrpcServerRequest(ServerCall<?, ?> call, Metadata headers) {
    this.call = requireNonNull(call, "call");
    this.headers = requireNonNull(headers, "headers");
  }

  /**
   * Returns the {@linkplain ServerCall server call} passed to {@link
   * ServerInterceptor#interceptCall}.
   */
  public ServerCall<?, ?> call() {
    return call;
  }

  /**
   * Returns {@linkplain ServerCall#getMethodDescriptor()}} from the {@link #call()}.
   */
  public MethodDescriptor<?, ?> methodDescriptor() {
    return call.getMethodDescriptor();
  }

  /**
   * Returns the {@linkplain Metadata headers} passed to {@link ServerInterceptor#interceptCall}.
   */
  public Metadata headers() {
    return headers;
  }
}

package net.devh.boot.grpc.server.security.interceptors;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

import static java.util.Objects.requireNonNull;

/**
 * Allows access gRPC specific aspects of a server request during sampling and parsing.
 *
 * @author Sajad Mehrabi (mehrabisajad@gmail.com)
 */
public record GrpcServerRequest(ServerCall<?, ?> call, Metadata headers) {
  public GrpcServerRequest(ServerCall<?, ?> call, Metadata headers) {
    this.call = requireNonNull(call, "call");
    this.headers = requireNonNull(headers, "headers");
  }

  /**
   * Returns {@linkplain ServerCall#getMethodDescriptor()}} from the {@link #call()}.
   */
  public MethodDescriptor<?, ?> methodDescriptor() {
    return call.getMethodDescriptor();
  }

}
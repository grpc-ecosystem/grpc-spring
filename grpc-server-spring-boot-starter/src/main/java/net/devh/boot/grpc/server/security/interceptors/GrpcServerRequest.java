/*
 * Copyright (c) 2016-2024 The gRPC-Spring Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.devh.boot.grpc.server.security.interceptors;

import static java.util.Objects.requireNonNull;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

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

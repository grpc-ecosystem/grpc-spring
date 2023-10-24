/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
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

package net.devh.boot.grpc.server.interceptor;

import static java.util.Objects.requireNonNull;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * A server interceptor wrapper that assigns an order to the underlying server interceptor.
 *
 * @deprecated Use the original {@link ServerInterceptor} in combination with {@link Order} (either on the target class
 *             itself or the related factory method).
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
@Deprecated
public class OrderedServerInterceptor implements ServerInterceptor, Ordered {

    private final ServerInterceptor serverInterceptor;
    private final int order;

    /**
     * Creates a new OrderedServerInterceptor with the given server interceptor and order.
     *
     * @param serverInterceptor The server interceptor to delegate to.
     * @param order The order of this interceptor.
     */
    public OrderedServerInterceptor(final ServerInterceptor serverInterceptor, final int order) {
        this.serverInterceptor = requireNonNull(serverInterceptor, "serverInterceptor");
        this.order = order;
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, final Metadata headers,
            final ServerCallHandler<ReqT, RespT> next) {
        return this.serverInterceptor.interceptCall(call, headers, next);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return "OrderedServerInterceptor [interceptor=" + this.serverInterceptor + ", order=" + this.order + "]";
    }

}

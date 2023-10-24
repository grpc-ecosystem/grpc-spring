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

package net.devh.boot.grpc.client.interceptor;

import static java.util.Objects.requireNonNull;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;

/**
 * A client interceptor wrapper that assigns an order to the underlying client interceptor.
 *
 * @deprecated Use the original {@link ClientInterceptor} in combination with {@link Order} (either on the target class
 *             itself or the related factory method).
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
@Deprecated
public class OrderedClientInterceptor implements ClientInterceptor, Ordered {

    private final ClientInterceptor clientInterceptor;
    private final int order;

    /**
     * Creates a new OrderedClientInterceptor with the given client interceptor and order.
     *
     * @param clientInterceptor The client interceptor to delegate to.
     * @param order The order of this interceptor.
     */
    public OrderedClientInterceptor(ClientInterceptor clientInterceptor, int order) {
        this.clientInterceptor = requireNonNull(clientInterceptor, "clientInterceptor");
        this.order = order;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        return this.clientInterceptor.interceptCall(method, callOptions, next);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return "OrderedClientInterceptor [interceptor=" + this.clientInterceptor + ", order=" + this.order + "]";
    }

}

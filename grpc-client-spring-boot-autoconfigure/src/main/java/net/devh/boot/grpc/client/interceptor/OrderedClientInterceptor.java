/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.client.interceptor;

import static java.util.Objects.requireNonNull;

import org.springframework.core.Ordered;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;

/**
 * A client interceptor wrapper that assigns an order to the underlying client interceptor.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
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

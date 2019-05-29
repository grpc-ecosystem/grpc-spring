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

package net.devh.boot.grpc.server.interceptor;

import static java.util.Objects.requireNonNull;

import org.springframework.core.Ordered;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * A server interceptor wrapper that assigns an order to the underlying server interceptor.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class OrderedServerInterceptor implements ServerInterceptor, Ordered {

    private final ServerInterceptor serverInterceptor;
    private final int order;

    /**
     * Creates a new OrderedServerInterceptor with the given server interceptor and order.
     *
     * @param serverInterceptor The server interceptor to delegate to.
     * @param order The order of this interceptor.
     */
    public OrderedServerInterceptor(ServerInterceptor serverInterceptor, int order) {
        this.serverInterceptor = requireNonNull(serverInterceptor, "serverInterceptor");
        this.order = order;
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
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

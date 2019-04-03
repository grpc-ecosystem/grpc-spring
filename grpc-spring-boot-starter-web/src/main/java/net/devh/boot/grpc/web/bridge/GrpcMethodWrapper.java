/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.web.bridge;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.PrototypeMarshaller;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerMethodDefinition;
import io.grpc.Status;

public class GrpcMethodWrapper<RequestT extends Message, ResponseT extends Message>
        implements ServerCallHandler<RequestT, ResponseT> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static GrpcMethodWrapper<?, ?> ofRaw(final ServerMethodDefinition method) {
        return of(method);
    }

    public static <RequestT extends Message, ResponseT extends Message> GrpcMethodWrapper<RequestT, ResponseT> of(
            final ServerMethodDefinition<RequestT, ResponseT> method) {
        final MethodDescriptor<RequestT, ResponseT> methodDescriptor = method.getMethodDescriptor();
        return new GrpcMethodWrapper<>(
                method.getMethodDescriptor(),
                getRequestBuilderFor(methodDescriptor),
                getRequestDescriptorFor(methodDescriptor),
                method.getServerCallHandler());
    }

    protected static <RequestT extends Message> Supplier<Message.Builder> getRequestBuilderFor(
            final MethodDescriptor<RequestT, ?> method) {
        final RequestT requestPrototype = getRequestPrototypeFor(method);
        return requestPrototype::newBuilderForType;
    }

    protected static <RequestT extends Message> Descriptor getRequestDescriptorFor(
            final MethodDescriptor<RequestT, ?> method) {
        final RequestT requestPrototype = getRequestPrototypeFor(method);
        return requestPrototype.getDescriptorForType();
    }

    @SuppressWarnings("unchecked")
    protected static <RequestT extends Message> RequestT getRequestPrototypeFor(
            final MethodDescriptor<RequestT, ?> method) {
        final PrototypeMarshaller<?> requestMarshaller = (PrototypeMarshaller<RequestT>) method.getRequestMarshaller();
        return (RequestT) requestMarshaller.getMessagePrototype();
    }

    private final MethodDescriptor<RequestT, ResponseT> methodDescriptor;
    private final Supplier<? extends Message.Builder> requestBuilderSupplier;
    private final Descriptor requestDescriptor;
    private final ServerCallHandler<RequestT, ResponseT> delegate;

    public GrpcMethodWrapper(final MethodDescriptor<RequestT, ResponseT> methodDescriptor,
            final Supplier<? extends Message.Builder> requestBuilderSupplier,
            final Descriptor requestDescriptor,
            final ServerCallHandler<RequestT, ResponseT> delegate) {
        this.methodDescriptor = methodDescriptor;
        this.requestBuilderSupplier = requestBuilderSupplier;
        this.requestDescriptor = requestDescriptor;
        this.delegate = delegate;
    }

    public GrpcMethodWrapper<RequestT, ResponseT> intercept(final ServerInterceptor interceptor) {
        return new GrpcMethodWrapper<>(this.methodDescriptor, this.requestBuilderSupplier, this.requestDescriptor,
                InterceptCallHandler.create(interceptor, this.delegate));
    }

    public WrappedServerCall<RequestT, ResponseT> prepare() {
        return new WrappedServerCall<>(this.methodDescriptor);
    }

    @Override
    public Listener<RequestT> startCall(final ServerCall<RequestT, ResponseT> call, final Metadata headers) {
        return this.delegate.startCall(call, headers);
    }

    public Supplier<? extends Message.Builder> getRequestBuilderSupplier() {
        return this.requestBuilderSupplier;
    }

    public Descriptor getRequestDescriptor() {
        return this.requestDescriptor;
    }

    public String getFullMethodName() {
        return this.methodDescriptor.getFullMethodName();
    }

    static final class InterceptCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {

        public static <ReqT, RespT> InterceptCallHandler<ReqT, RespT> create(
                final ServerInterceptor interceptor, final ServerCallHandler<ReqT, RespT> callHandler) {
            return new InterceptCallHandler<>(interceptor, callHandler);
        }

        private final ServerInterceptor interceptor;
        private final ServerCallHandler<ReqT, RespT> callHandler;

        private InterceptCallHandler(final ServerInterceptor interceptor,
                final ServerCallHandler<ReqT, RespT> callHandler) {
            this.interceptor = requireNonNull(interceptor, "interceptor");
            this.callHandler = callHandler;
        }

        @Override
        public ServerCall.Listener<ReqT> startCall(final ServerCall<ReqT, RespT> call, final Metadata headers) {
            return this.interceptor.interceptCall(call, headers, this.callHandler);
        }

    }

    static final class WrappedServerCall<RequestT, ResponseT> extends ServerCall<RequestT, ResponseT> {

        private final MethodDescriptor<RequestT, ResponseT> methodDescriptor;

        private Status status;
        private Metadata headers;
        private final List<ResponseT> messages = new ArrayList<>(2);

        public WrappedServerCall(final MethodDescriptor<RequestT, ResponseT> methodDescriptor) {
            this.methodDescriptor = methodDescriptor;
        }

        @Override
        public void request(final int numMessages) {
            // Does nothing
        }

        @Override
        public void sendHeaders(final Metadata headers) {
            if (this.headers != null) {
                throw new IllegalStateException("Headers already send");
            }
            this.headers = headers;
        }

        @Override
        public void sendMessage(final ResponseT message) {
            this.messages.add(message);
        }

        @Override
        public void close(final Status status, final Metadata trailers) {
            this.status = status;
            if (this.headers == null) {
                this.headers = trailers;
            } else {
                this.headers.merge(trailers);
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public MethodDescriptor<RequestT, ResponseT> getMethodDescriptor() {
            return this.methodDescriptor;
        }

        public GrpcMethodResult<ResponseT> getResult() {
            if (this.status == null) {
                throw new IllegalStateException("Call not yet closed!");
            }
            return new GrpcMethodResult<>(this.status, this.headers, this.messages);
        }

    }

}

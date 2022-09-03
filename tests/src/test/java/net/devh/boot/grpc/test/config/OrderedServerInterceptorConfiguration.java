/*
 * Copyright (c) 2016-2022 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.test.config;

import javax.annotation.Priority;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@Configuration
public class OrderedServerInterceptorConfiguration {

    @GrpcGlobalServerInterceptor
    @Priority(30)
    @Component("SecondPriorityAnnotatedInterceptor")
    public class SecondPriorityAnnotatedInterceptor extends TestServerInterceptor {
    }

    @GrpcGlobalServerInterceptor
    @Order(20)
    @Component("SecondOrderAnnotatedInterceptor")
    public class SecondOrderAnnotatedInterceptor extends TestServerInterceptor {
    }

    @GrpcGlobalServerInterceptor
    @Component("FirstOrderedInterfaceInterceptor")
    public class FirstOrderedInterfaceInterceptor extends TestServerInterceptor implements Ordered {
        @Override
        public int getOrder() {
            return 40;
        }
    }

    @GrpcGlobalServerInterceptor
    @Order(10)
    @Component("FirstOrderAnnotatedInterceptor")
    public class FirstOrderAnnotatedInterceptor extends TestServerInterceptor {
    }

    @GrpcGlobalServerInterceptor
    @Component("SecondOrderedInterfaceInterceptor")
    public class SecondOrderedInterfaceInterceptor extends TestServerInterceptor implements Ordered {
        @Override
        public int getOrder() {
            return 50;
        }
    }


    @GrpcGlobalServerInterceptor
    @Priority(5)
    @Component("FirstPriorityAnnotatedInterceptor")
    public class FirstPriorityAnnotatedInterceptor extends TestServerInterceptor {
    }

    @GrpcGlobalServerInterceptor
    @Order(30)
    ServerInterceptor firstOrderAnnotationInterceptorBean() {
        return new TestServerInterceptor();
    }

    @GrpcGlobalServerInterceptor
    @Order(75)
    ServerInterceptor secondOrderAnnotationInterceptorBean() {
        return new TestServerInterceptor();
    }

    public static class TestServerInterceptor implements ServerInterceptor, BeanNameAware {

        private String name;

        @Override
        public void setBeanName(final String name) {
            this.name = name;
        }

        @Override
        public final <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                final ServerCall<ReqT, RespT> call,
                final Metadata headers,
                final ServerCallHandler<ReqT, RespT> next) {
            return next.startCall(call, headers);
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

}

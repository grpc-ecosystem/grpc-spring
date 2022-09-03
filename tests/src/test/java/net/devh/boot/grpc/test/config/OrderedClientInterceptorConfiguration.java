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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

@Configuration
public class OrderedClientInterceptorConfiguration {

    @GrpcGlobalClientInterceptor
    @Component("SecondPriorityAnnotatedInterceptor")
    @Priority(30)
    public class SecondPriorityAnnotatedInterceptor extends TestClientInterceptor {
    }

    @GrpcGlobalClientInterceptor
    @Component("SecondOrderAnnotatedInterceptor")
    @Order(20)
    public class SecondOrderAnnotatedInterceptor extends TestClientInterceptor {
    }

    @GrpcGlobalClientInterceptor
    @Component("FirstOrderedInterfaceInterceptor")
    public class FirstOrderedInterfaceInterceptor extends TestClientInterceptor implements Ordered {
        @Override
        public int getOrder() {
            return 40;
        }
    }

    @GrpcGlobalClientInterceptor
    @Order(10)
    @Component("FirstOrderAnnotatedInterceptor")
    public class FirstOrderAnnotatedInterceptor extends TestClientInterceptor {
    }

    @GrpcGlobalClientInterceptor
    @Component("SecondOrderedInterfaceInterceptor")
    public class SecondOrderedInterfaceInterceptor extends TestClientInterceptor implements Ordered {
        @Override
        public int getOrder() {
            return 50;
        }
    }

    @GrpcGlobalClientInterceptor
    @Priority(5)
    @Component("FirstPriorityAnnotatedInterceptor")
    public class FirstPriorityAnnotatedInterceptor extends TestClientInterceptor {
    }

    @GrpcGlobalClientInterceptor
    @Order(30)
    ClientInterceptor firstOrderAnnotationInterceptorBean() {
        return new TestClientInterceptor();
    }

    @GrpcGlobalClientInterceptor
    @Order(75)
    ClientInterceptor secondOrderAnnotationInterceptorBean() {
        return new TestClientInterceptor();
    }

    private static class TestClientInterceptor implements ClientInterceptor, BeanNameAware {

        private String name;

        @Override
        public void setBeanName(final String name) {
            this.name = name;
        }

        @Override
        public final <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method,
                final CallOptions callOptions, final Channel next) {
            return next.newCall(method, callOptions);
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

}

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

package net.devh.boot.grpc.test.config;

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
    @Order(30)
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
    @Order(5)
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

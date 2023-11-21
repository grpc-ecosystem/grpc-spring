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

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@Configuration
public class OrderedServerInterceptorConfiguration {

    @GrpcGlobalServerInterceptor
    @Order(30)
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
    @Order(5)
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

    private static class TestServerInterceptor implements ServerInterceptor, BeanNameAware {

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

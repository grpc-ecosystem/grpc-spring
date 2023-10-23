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

package net.devh.boot.grpc.client.inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;

/**
 * Tests for {@link GrpcClientBeanPostProcessor}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@SpringBootTest(classes = GrpcClientBeanPostProcessorTest.TestConfig.class)
@DirtiesContext
@ImportAutoConfiguration(GrpcClientAutoConfiguration.class)
class GrpcClientBeanPostProcessorTest {

    @Autowired
    GrpcClientBeanPostProcessor postProcessor;

    @Autowired
    Interceptor1 interceptor1;

    @Test
    void testInterceptorsFromAnnotation1() {
        final List<ClientInterceptor> beans =
                assertDoesNotThrow(() -> this.postProcessor.interceptorsFromAnnotation(new GrpcClient() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return GrpcClient.class;
                    }

                    @Override
                    public String value() {
                        return "test";
                    }

                    @Override
                    public boolean sortInterceptors() {
                        return false;
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public Class<? extends ClientInterceptor>[] interceptors() {
                        return new Class[] {Interceptor1.class};
                    }

                    @Override
                    public String[] interceptorNames() {
                        return new String[0];
                    }

                }));

        assertThat(beans).containsExactly(this.interceptor1);
    }

    @Test
    void testInterceptorsFromAnnotation2() {
        final List<ClientInterceptor> beans =
                assertDoesNotThrow(() -> this.postProcessor.interceptorsFromAnnotation(new GrpcClient() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return GrpcClient.class;
                    }

                    @Override
                    public String value() {
                        return "test";
                    }

                    @Override
                    public boolean sortInterceptors() {
                        return false;
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public Class<? extends ClientInterceptor>[] interceptors() {
                        return new Class[] {Interceptor2.class};
                    }

                    @Override
                    public String[] interceptorNames() {
                        return new String[0];
                    }

                }));

        assertThat(beans).hasSize(1).doesNotContain(this.interceptor1);
    }

    static class TestConfig {

        @Bean
        Interceptor1 interceptor1() {
            return new Interceptor1();
        }


    }

    public static class Interceptor1 implements ClientInterceptor {

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method,
                final CallOptions callOptions, final Channel next) {
            return next.newCall(method, callOptions);
        }

    }

    public static class Interceptor2 implements ClientInterceptor {

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method,
                final CallOptions callOptions, final Channel next) {
            return next.newCall(method, callOptions);
        }

    }

}

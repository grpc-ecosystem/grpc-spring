/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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
                    public String beanName() {
                        return "";
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
                    public String beanName() {
                        return "";
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

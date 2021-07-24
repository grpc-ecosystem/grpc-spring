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

package net.devh.boot.grpc.test.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.interceptor.AnnotationGlobalClientInterceptorConfigurer;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

/**
 * Tests that {@link GrpcGlobalClientInterceptor}, {@link GlobalClientInterceptorConfigurer} and
 * {@link GlobalClientInterceptorRegistry} work as expected.
 */
@SpringBootTest
class PickupClientInterceptorTest {

    @Autowired
    AnnotationGlobalClientInterceptorConfigurer annotationGlobalClientInterceptorConfigurer;

    @Autowired
    GlobalClientInterceptorRegistry globalClientInterceptorRegistry;

    @Test
    void test() {
        final List<ClientInterceptor> interceptors = new ArrayList<>();
        this.annotationGlobalClientInterceptorConfigurer.configureClientInterceptors(interceptors);
        assertThat(interceptors).containsExactlyInAnyOrder(
                new ConfigAnnotatedClientInterceptor(),
                new ClassAnnotatedClientInterceptor());

        assertThat(this.globalClientInterceptorRegistry.getClientInterceptors()).containsExactlyInAnyOrder(
                new ConfigAnnotatedClientInterceptor(),
                new ClassAnnotatedClientInterceptor(),
                new ConfigurerClientInterceptor());

    }

    @SpringBootConfiguration
    @ImportAutoConfiguration(GrpcClientAutoConfiguration.class)
    @ComponentScan(basePackageClasses = ClassAnnotatedClientInterceptor.class)
    public static class TestConfig {

        @GrpcGlobalClientInterceptor
        ConfigAnnotatedClientInterceptor configAnnotatedClientInterceptor() {
            return new ConfigAnnotatedClientInterceptor();
        }

        @Bean
        GlobalClientInterceptorConfigurer globalClientInterceptorConfigurer() {
            return interceptors -> interceptors.add(new ConfigurerClientInterceptor());
        }

    }

    /**
     * Simple No-Op ClientInterceptor for testing purposes.
     */
    static class NoOpClientInterceptor implements ClientInterceptor {

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method,
                final CallOptions callOptions,
                final Channel next) {
            return next.newCall(method, callOptions);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

        @Override
        // Fake equality for test simplifications
        public boolean equals(final Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }

    }

    /**
     * Used to check that {@link GrpcGlobalClientInterceptor} works in {@link Configuration}s.
     */
    static class ConfigAnnotatedClientInterceptor extends NoOpClientInterceptor {
    }

    /**
     * Used to check that {@link GrpcGlobalClientInterceptor} works on bean classes themselves.
     */
    @GrpcGlobalClientInterceptor
    static class ClassAnnotatedClientInterceptor extends NoOpClientInterceptor {
    }

    /**
     * Used to check that {@link GlobalClientInterceptorConfigurer} work.
     */
    @Component
    static class ConfigurerClientInterceptor extends NoOpClientInterceptor {
    }

    /**
     * Used to check that {@link ClientInterceptor} aren't picked up randomly.
     */
    @Component
    static class DoNotPickMeUpClientInterceptor extends NoOpClientInterceptor {
    }

}

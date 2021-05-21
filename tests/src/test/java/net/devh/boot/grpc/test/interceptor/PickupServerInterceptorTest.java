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

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.interceptor.AnnotationGlobalServerInterceptorConfigurer;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.scope.GrpcRequestScope;

/**
 * Tests that {@link GrpcGlobalServerInterceptor}, {@link GlobalServerInterceptorConfigurer} and
 * {@link GlobalServerInterceptorRegistry} work as expected.
 */
@SpringBootTest
class PickupServerInterceptorTest {

    @Autowired
    AnnotationGlobalServerInterceptorConfigurer annotationGlobalServerInterceptorConfigurer;

    @Autowired
    GlobalServerInterceptorRegistry globalServerInterceptorRegistry;

    @Autowired
    GrpcRequestScope grpcRequestScope;

    @Test
    void test() {
        final List<ServerInterceptor> interceptors = new ArrayList<>();
        this.annotationGlobalServerInterceptorConfigurer.configureServerInterceptors(interceptors);
        assertThat(interceptors).containsExactlyInAnyOrder(
                new ConfigAnnotatedServerInterceptor(),
                new ClassAnnotatedServerInterceptor(),
                this.grpcRequestScope);

        assertThat(this.globalServerInterceptorRegistry.getServerInterceptors()).containsExactlyInAnyOrder(
                new ConfigAnnotatedServerInterceptor(),
                new ClassAnnotatedServerInterceptor(),
                new ConfigurerServerInterceptor(),
                this.grpcRequestScope);

    }

    @SpringBootConfiguration
    @ImportAutoConfiguration(GrpcServerAutoConfiguration.class)
    @ComponentScan(basePackageClasses = ClassAnnotatedServerInterceptor.class)
    public static class TestConfig {

        @GrpcGlobalServerInterceptor
        ConfigAnnotatedServerInterceptor configAnnotatedServerInterceptor() {
            return new ConfigAnnotatedServerInterceptor();
        }

        @Bean
        GlobalServerInterceptorConfigurer globalServerInterceptorConfigurer() {
            return interceptors -> interceptors.add(new ConfigurerServerInterceptor());
        }

    }

    /**
     * Simple No-Op ServerInterceptor for testing purposes.
     */
    static class NoOpServerInterceptor implements ServerInterceptor {

        @Override
        public <ReqT, RespT> Listener<ReqT> interceptCall(
                final ServerCall<ReqT, RespT> call,
                final Metadata headers,
                final ServerCallHandler<ReqT, RespT> next) {
            return next.startCall(call, headers);
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
     * Used to check that {@link GrpcGlobalServerInterceptor} works in {@link Configuration}s.
     */
    static class ConfigAnnotatedServerInterceptor extends NoOpServerInterceptor {
    }

    /**
     * Used to check that {@link GrpcGlobalServerInterceptor} works on bean classes themselves.
     */
    @GrpcGlobalServerInterceptor
    static class ClassAnnotatedServerInterceptor extends NoOpServerInterceptor {
    }

    /**
     * Used to check that {@link GlobalServerInterceptorConfigurer} work.
     */
    @Component
    static class ConfigurerServerInterceptor extends NoOpServerInterceptor {
    }

    /**
     * Used to check that {@link ServerInterceptor} aren't picked up randomly.
     */
    @Component
    static class DoNotPickMeUpServerInterceptor extends NoOpServerInterceptor {
    }

}

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

package net.devh.boot.grpc.examples.cloud.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.Span;
import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import zipkin2.reporter.Reporter;

/**
 * @description:
 * @author: haochencheng
 * @create: 2021-07-24 16:38
 **/
@ConditionalOnProperty(value = {"spring.sleuth.enabled", "spring.zipkin.enabled"}, havingValue = "true")
@Configuration
@Slf4j
public class GrpcSleuthClientConfig {

    @Bean
    public GrpcTracing grpcTracing(Tracing tracing) {
        return GrpcTracing.create(tracing);
    }

    /**
     * We also create a client-side interceptor and put that in the context, this interceptor can then be injected into
     * gRPC clients and then applied to the managed channel.
     * 
     * @param grpcTracing
     * @return
     */
    @Bean
    ClientInterceptor grpcClientSleuthInterceptor(GrpcTracing grpcTracing) {
        return grpcTracing.newClientInterceptor();
    }

    @Bean
    ServerInterceptor grpcServerSleuthInterceptor(GrpcTracing grpcTracing) {
        return grpcTracing.newServerInterceptor();
    }

    /**
     * Use this for debugging (or if there is no Zipkin server running on port 9411)
     * 
     * @return
     */
    @Bean
    public Reporter<Span> spanReporter() {
        return span -> {
            if (log.isDebugEnabled()) {
                log.debug("{}", span);
            }
        };
    }

    @Bean
    public GlobalClientInterceptorConfigurer globalInterceptorConfigurerAdapter(
            ClientInterceptor grpcClientSleuthInterceptor) {
        return registry -> registry.add(grpcClientSleuthInterceptor);
    }


}

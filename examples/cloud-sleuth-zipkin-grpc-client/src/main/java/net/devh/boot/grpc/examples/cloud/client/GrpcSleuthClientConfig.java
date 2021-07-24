package net.devh.boot.grpc.examples.cloud.client;

import brave.Span;
import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.Reporter;

/**
 * @description:
 * @author: haochencheng
 * @create: 2021-07-24 16:38
 **/
@ConditionalOnProperty(value = { "spring.sleuth.enabled", "spring.zipkin.enabled" }, havingValue = "true")
@Configuration
@Slf4j
public class GrpcSleuthClientConfig {

    @Bean
    public GrpcTracing grpcTracing(Tracing tracing) {
        return GrpcTracing.create(tracing);
    }

    /**
     * We also create a client-side interceptor and put that in the context, this interceptor can then be injected into gRPC clients and
     * then applied to the managed channel.
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
    public GlobalClientInterceptorConfigurer globalInterceptorConfigurerAdapter(ClientInterceptor grpcClientSleuthInterceptor) {
        return registry -> registry.add(grpcClientSleuthInterceptor);
    }


}

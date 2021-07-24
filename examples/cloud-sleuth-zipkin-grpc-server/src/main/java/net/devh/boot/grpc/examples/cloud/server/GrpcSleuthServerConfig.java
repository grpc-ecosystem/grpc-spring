package net.devh.boot.grpc.examples.cloud.server;

import brave.Span;
import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
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
public class GrpcSleuthServerConfig {

    @Bean
    public GrpcTracing grpcTracing(Tracing tracing) {
        return GrpcTracing.create(tracing);
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
    @ConditionalOnProperty(value = "sample.zipkin.enabled", havingValue = "false")
    public Reporter<Span> spanReporter() {
        return span -> {
            if (log.isDebugEnabled()) {
                log.debug("{}", span);
            }
        };
    }

    @Bean
    public GlobalServerInterceptorConfigurer globalInterceptorConfigurerAdapter(ServerInterceptor grpcServerSleuthInterceptor) {
        return registry -> registry.add(grpcServerSleuthInterceptor);
    }

}

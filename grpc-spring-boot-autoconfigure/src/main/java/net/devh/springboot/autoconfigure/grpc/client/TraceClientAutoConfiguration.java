package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@ConditionalOnProperty(value = "spring.sleuth.scheduled.enabled", matchIfMissing = true)
@ConditionalOnClass(Tracer.class)
//@ConditionalOnBean(Tracer.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceClientAutoConfiguration {

    @Bean
    public GlobalClientInterceptorConfigurerAdapter globalTraceClientInterceptorConfigurerAdapter(Tracer tracer) {
        return new GlobalClientInterceptorConfigurerAdapter() {

            @Override
            public void addClientInterceptors(GlobalClientInterceptorRegistry registry) {
                registry.addClientInterceptors(new TraceClientInterceptor(tracer, new MetadataInjector()));
            }
        };
    }
}
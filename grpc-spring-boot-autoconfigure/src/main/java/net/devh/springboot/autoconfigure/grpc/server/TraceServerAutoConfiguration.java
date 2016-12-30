package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "spring.sleuth.scheduled.enabled", matchIfMissing = true)
@ConditionalOnClass(Tracer.class)
//@ConditionalOnBean(Tracer.class)
public class TraceServerAutoConfiguration {

    @Bean
    public GlobalServerInterceptorConfigurerAdapter globalTraceServerInterceptorConfigurerAdapter(Tracer tracer) {
        return new GlobalServerInterceptorConfigurerAdapter() {
            @Override
            public void addServerInterceptors(GlobalServerInterceptorRegistry registry) {
                registry.addServerInterceptors(new TraceServerInterceptor(tracer, new MetadataExtractor()));
            }
        };
    }

}
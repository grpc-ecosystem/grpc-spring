package net.devh.examples.grpc.local;

import net.devh.springboot.autoconfigure.grpc.server.GlobalServerInterceptorConfigurer;
import net.devh.springboot.autoconfigure.grpc.server.GlobalServerInterceptorRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalInterceptorConfiguration {

    @Bean
    public GlobalServerInterceptorConfigurer globalInterceptorConfigurerAdapter() {
        return new GlobalServerInterceptorConfigurer() {
            @Override
            public void addServerInterceptors(GlobalServerInterceptorRegistry registry) {
                registry.addServerInterceptors(new LogGrpcInterceptor());
            }
        };
    }

}
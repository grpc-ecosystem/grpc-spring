package net.devh.examples.grpc.cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.devh.springboot.autoconfigure.grpc.server.GlobalServerInterceptorConfigurer;

@Configuration
public class GlobalInterceptorConfiguration {

    @Bean
    public GlobalServerInterceptorConfigurer globalInterceptorConfigurerAdapter() {
        return registry -> registry.addServerInterceptors(new LogGrpcInterceptor());
    }

}

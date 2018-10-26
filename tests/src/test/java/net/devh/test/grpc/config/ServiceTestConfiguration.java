package net.devh.test.grpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.devh.test.grpc.server.TestServiceImpl;

@Configuration
public class ServiceTestConfiguration {

    @Bean
    TestServiceImpl testService() {
        return new TestServiceImpl();
    }

}

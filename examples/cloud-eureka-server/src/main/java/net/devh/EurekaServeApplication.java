package net.devh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServeApplication.class, args);
    }
}

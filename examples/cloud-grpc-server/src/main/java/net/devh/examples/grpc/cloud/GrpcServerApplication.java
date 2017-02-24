package net.devh.examples.grpc.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 2016/11/8
 */
@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
public class GrpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcServerApplication.class, args);
    }
}

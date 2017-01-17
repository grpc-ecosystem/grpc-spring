package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(ConsulDiscoveryProperties.class)
public class GrpcMetedataConsulConfiguration {

    @Autowired
    private ConsulDiscoveryProperties properties;

    @Autowired
    private GrpcServerProperties grpcProperties;

    @PostConstruct
    public void init() {
        this.properties.getTags().add("grpc=" + grpcProperties.getPort());
    }

}

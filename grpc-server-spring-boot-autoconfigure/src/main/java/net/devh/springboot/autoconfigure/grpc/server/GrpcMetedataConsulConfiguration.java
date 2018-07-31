package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecwid.consul.v1.ConsulClient;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({ConsulDiscoveryProperties.class, ConsulClient.class,GrpcServerProperties.class})
public class GrpcMetedataConsulConfiguration  {

    @ConditionalOnMissingBean
    @Bean
    public ConsulRegistrationCustomizer consulGrpcRegistrationCustomizer(GrpcServerProperties grpcServerProperties) {
        return new ConsulGrpcRegistrationCustomizer(grpcServerProperties);
    }
}

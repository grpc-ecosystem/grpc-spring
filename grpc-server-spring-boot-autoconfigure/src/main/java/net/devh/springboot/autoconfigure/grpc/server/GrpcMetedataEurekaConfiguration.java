package net.devh.springboot.autoconfigure.grpc.server;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Configuration;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.discovery.EurekaClient;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({EurekaInstanceConfigBean.class, EurekaClient.class})
public class GrpcMetedataEurekaConfiguration {

    @Autowired(required = false)
    private EurekaInstanceConfig instance;

    @Autowired
    private GrpcServerProperties grpcProperties;

    @PostConstruct
    public void init() {
        if (this.instance == null) {
            return;
        }
        this.instance.getMetadataMap().put("gRPC.port", String.valueOf(grpcProperties.getPort()));
    }
}
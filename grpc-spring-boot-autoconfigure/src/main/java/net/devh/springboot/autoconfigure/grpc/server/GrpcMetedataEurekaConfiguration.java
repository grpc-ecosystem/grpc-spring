package net.devh.springboot.autoconfigure.grpc.server;

import com.netflix.appinfo.EurekaInstanceConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(EurekaInstanceConfigBean.class)
public class GrpcMetedataEurekaConfiguration {

    @Autowired
    private EurekaInstanceConfig instance;

    @Autowired
    private GrpcServerProperties grpcProperties;

    @PostConstruct
    public void init() {
        this.instance.getMetadataMap().put("grpc", String.valueOf(grpcProperties.getPort()));
    }
}
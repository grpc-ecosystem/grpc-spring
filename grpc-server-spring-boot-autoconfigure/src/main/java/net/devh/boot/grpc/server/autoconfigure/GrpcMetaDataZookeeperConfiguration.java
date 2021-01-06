package net.devh.boot.grpc.server.autoconfigure;

import net.devh.boot.grpc.common.util.GrpcUtils;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author z00567810
 * @date 2021/1/6
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({ZookeeperRegistration.class})
public class GrpcMetaDataZookeeperConfiguration {

    @Autowired(required = false)
    ZookeeperRegistration zookeeperRegistration;

    @Autowired
    private GrpcServerProperties grpcServerProperties;


    @PostConstruct
    public void init() {
        final String port = String.valueOf(grpcServerProperties.getPort());
        zookeeperRegistration.setPort(0);
        if (!GrpcUtils.INTER_PROCESS_DISABLE.equals(port)) {
            zookeeperRegistration.getServiceInstance().getPayload().getMetadata().put(GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT, port);
        }
    }
}

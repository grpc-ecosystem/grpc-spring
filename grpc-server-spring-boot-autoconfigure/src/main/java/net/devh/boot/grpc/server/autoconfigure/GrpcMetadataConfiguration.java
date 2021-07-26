package net.devh.boot.grpc.server.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.util.GrpcUtils;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass({Registration.class})
@Slf4j
public class GrpcMetadataConfiguration {

    @Autowired(required = false)
    private GrpcServerProperties grpcProperties;

    @EventListener(InstancePreRegisteredEvent.class)
    public void beforeRegistered(InstancePreRegisteredEvent event) {
        Registration registration = event.getRegistration();
        if (registration != null) {
            final int port = grpcProperties.getPort();
            if (GrpcUtils.INTER_PROCESS_DISABLE != port) {
                registration.getMetadata()
                        .put(GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT, Integer.toString(port));

                log.info("add  metadata to {}", registration.getClass().getName());
            }
        }
    }

}

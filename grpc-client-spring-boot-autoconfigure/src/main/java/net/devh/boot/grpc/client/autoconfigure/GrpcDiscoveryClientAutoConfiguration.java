package net.devh.boot.grpc.client.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.NameResolverProvider;
import net.devh.boot.grpc.client.nameresolver.DiscoveryClientResolverFactory;

@Configuration
@ConditionalOnBean(DiscoveryClient.class)
@AutoConfigureBefore(GrpcClientAutoConfiguration.class)
public class GrpcDiscoveryClientAutoConfiguration {

    @Bean
    NameResolverProvider discoveryNameResolverProvider(final DiscoveryClient client) {
        return new DiscoveryClientResolverFactory(client);
    }

}

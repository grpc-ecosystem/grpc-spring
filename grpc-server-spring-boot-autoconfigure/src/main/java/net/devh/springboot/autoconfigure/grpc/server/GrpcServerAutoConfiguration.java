package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.Server;
import io.netty.channel.Channel;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({Server.class, GrpcServerFactory.class})
public class GrpcServerAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerProperties defaultGrpcServerProperties() {
        return new GrpcServerProperties();
    }

    @Bean
    public GlobalServerInterceptorRegistry globalServerInterceptorRegistry() {
        return new GlobalServerInterceptorRegistry();
    }

    @ConditionalOnMissingBean
    @Bean
    public AnnotationGrpcServiceDiscoverer defaultGrpcServiceFinder() {
        return new AnnotationGrpcServiceDiscoverer();
    }

    @ConditionalOnMissingBean
    @ConditionalOnClass(Channel.class)
    @Bean
    public NettyGrpcServerFactory nettyGrpcServiceFactory(GrpcServerProperties properties, GrpcServiceDiscoverer discoverer) {
        NettyGrpcServerFactory factory = new NettyGrpcServerFactory(properties);
        for (GrpcServiceDefinition service : discoverer.findGrpcServices()) {
            factory.addService(service);
        }

        return factory;
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(GrpcServerFactory factory) {
        return new GrpcServerLifecycle(factory);
    }


    @Configuration
    @ConditionalOnProperty(value = "spring.sleuth.scheduled.enabled", matchIfMissing = true)
    @ConditionalOnClass(Tracer.class)
    protected static class TraceServerAutoConfiguration {

        @Bean
        public GlobalServerInterceptorConfigurerAdapter globalTraceServerInterceptorConfigurerAdapter(final Tracer tracer) {
            return new GlobalServerInterceptorConfigurerAdapter() {
                @Override
                public void addServerInterceptors(GlobalServerInterceptorRegistry registry) {
                    registry.addServerInterceptors(new TraceServerInterceptor(tracer, new MetadataExtractor()));
                }
            };
        }

    }
}

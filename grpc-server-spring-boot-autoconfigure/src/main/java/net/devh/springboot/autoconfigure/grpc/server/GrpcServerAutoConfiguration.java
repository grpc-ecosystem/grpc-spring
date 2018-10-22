package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.Server;
import io.grpc.services.HealthStatusManager;
import io.netty.channel.Channel;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

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
    @Bean
    public HealthStatusManager healthStatusManager() {
        return new HealthStatusManager();
    }

    @ConditionalOnMissingBean(GrpcServerFactory.class)
    @ConditionalOnClass(Channel.class)
    @Bean
    public NettyGrpcServerFactory nettyGrpcServiceFactory(final GrpcServerProperties properties,
            final GrpcServiceDiscoverer discoverer) {
        final NettyGrpcServerFactory factory = new NettyGrpcServerFactory(properties);
        for (final GrpcCodecDefinition codec : discoverer.findGrpcCodec()) {
            factory.addCodec(codec);
        }
        for (final GrpcServiceDefinition service : discoverer.findGrpcServices()) {
            factory.addService(service);
        }

        return factory;
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(final GrpcServerFactory factory) {
        return new GrpcServerLifecycle(factory);
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.sleuth.scheduled.enabled", matchIfMissing = true)
    @AutoConfigureAfter({TraceAutoConfiguration.class})
    @ConditionalOnClass(value = {Tracing.class, GrpcTracing.class, TraceAutoConfiguration.class})
    protected static class TraceServerAutoConfiguration {

        @Bean
        public GrpcTracing grpcTracing(final Tracing tracing) {
            return GrpcTracing.create(tracing);
        }

        @Bean
        public GlobalServerInterceptorConfigurerAdapter globalTraceServerInterceptorConfigurerAdapter(final GrpcTracing grpcTracing) {
            return new GlobalServerInterceptorConfigurerAdapter() {
                @Override
                public void addServerInterceptors(GlobalServerInterceptorRegistry registry) {
                    registry.addServerInterceptors(grpcTracing.newServerInterceptor());
                }
            };
        }

    }
}

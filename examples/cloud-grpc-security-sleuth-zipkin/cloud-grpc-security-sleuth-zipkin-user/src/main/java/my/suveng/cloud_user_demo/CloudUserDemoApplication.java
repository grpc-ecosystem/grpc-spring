package my.suveng.cloud_user_demo;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@SpringBootApplication
@Slf4j
@EnableAsync
public class CloudUserDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudUserDemoApplication.class, args);
    }

    /**
     * 创建 tracing, 查看官方其他模块的demo
     */
    @Bean
    public GrpcTracing grpcTracing(Tracing tracing) {
        return GrpcTracing.create(tracing);
    }

    /**
     * grpc server 开启sleuth链路追踪功能, 以下引用其他demo原文
     * grpc-spring-boot-starter provides @GrpcGlobalInterceptor to allow server-side interceptors to be registered with all
     * server stubs, we are just taking advantage of that to install the server-side gRPC tracer.
     */
    @Bean
    ServerInterceptor grpcServerSleuthInterceptor(GrpcTracing grpcTracing) {
        return grpcTracing.newServerInterceptor();
    }

    /**
     * 	grpc client开启sleuth链路追踪功能, 以下引用原文
     * 	We also create a client-side interceptor and put that in the context, this interceptor can then be injected into gRPC clients and
     * 	then applied to the managed channel.
     */
    @Bean
    ClientInterceptor grpcClientSleuthInterceptor(GrpcTracing grpcTracing) {
        return grpcTracing.newClientInterceptor();
    }

    /**
     * 看情况启用zikpin server, 这里不启用
     * Use this for debugging (or if there is no Zipkin server running on port 9411)
     */
    @Bean
    @ConditionalOnProperty(value = "sample.zipkin.enabled", havingValue = "false")
    public Reporter<Span> spanReporter() {
        return span -> log.info(span + "");
    }

    @Bean
    public GlobalServerInterceptorConfigurer globalInterceptorConfigurerAdapter(ServerInterceptor grpcServerSleuthInterceptor) {
        return registry -> {
            registry.add(grpcServerSleuthInterceptor);
        };
    }
}

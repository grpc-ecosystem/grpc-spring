package my.suveng.istiocloudpaydemo;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@SpringBootApplication
@Slf4j
public class IstioCloudPayDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(IstioCloudPayDemoApplication.class, args);
	}

	@Bean
	public GrpcTracing grpcTracing(Tracing tracing) {
		return GrpcTracing.create(tracing);
	}

	//grpc-spring-boot-starter provides @GrpcGlobalInterceptor to allow server-side interceptors to be registered with all
	//server stubs, we are just taking advantage of that to install the server-side gRPC tracer.
	//grpc server端开启中间拦截
	@Bean
	@GRpcGlobalInterceptor
	ServerInterceptor grpcServerSleuthInterceptor(GrpcTracing grpcTracing) {
		return grpcTracing.newServerInterceptor();
	}

	//We also create a client-side interceptor and put that in the context, this interceptor can then be injected into gRPC clients and
	//then applied to the managed channel.
	// grpc client端开启拦截
	@Bean
	ClientInterceptor grpcClientSleuthInterceptor(GrpcTracing grpcTracing) {
		return grpcTracing.newClientInterceptor();
	}

	// Use this for debugging (or if there is no Zipkin server running on port 9411)
	@Bean
	@ConditionalOnProperty(value = "sample.zipkin.enabled", havingValue = "false")
	public Reporter<Span> spanReporter() {
		return new Reporter<Span>() {
			@Override
			public void report(Span span) {
				log.info(span+"");
			}
		};
	}
}

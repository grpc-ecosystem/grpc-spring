package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration({GrpcServerAutoConfiguration.class, TraceServerAutoConfiguration.class, GrpcMetedataEurekaConfiguration.class, GrpcMetedataConsulConfiguration.class})
public @interface EnableGrpcServer {

}

package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration({GrpcClientAutoConfiguration.class, GrpcClientBeanPostProcessor.class, TraceClientAutoConfiguration.class})
public @interface EnableGrpcClient {

}

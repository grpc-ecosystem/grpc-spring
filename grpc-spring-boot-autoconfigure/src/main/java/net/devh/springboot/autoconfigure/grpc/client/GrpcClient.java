package net.devh.springboot.autoconfigure.grpc.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.grpc.ClientInterceptor;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 2016/12/7
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GrpcClient {

    String value();

    Class<? extends ClientInterceptor>[] interceptors() default {};
}
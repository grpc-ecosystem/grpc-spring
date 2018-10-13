package net.devh.springboot.autoconfigure.grpc.server.codec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 10/13/18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GrpcCodec {

    boolean advertised() default false;

    CodecType codecType() default CodecType.ALL;
}

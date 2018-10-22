package net.devh.springboot.autoconfigure.grpc.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import io.grpc.ClientInterceptor;

/**
 * Annotation for gRPC {@link ClientInterceptor}s to apply them globally.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GrpcGlobalClientInterceptor {
}

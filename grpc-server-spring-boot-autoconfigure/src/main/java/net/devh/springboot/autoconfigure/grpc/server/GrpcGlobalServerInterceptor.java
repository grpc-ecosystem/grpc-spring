package net.devh.springboot.autoconfigure.grpc.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import io.grpc.ServerInterceptor;

/**
 * Annotation for gRPC {@link ServerInterceptor}s to apply them globally.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GrpcGlobalServerInterceptor {
}

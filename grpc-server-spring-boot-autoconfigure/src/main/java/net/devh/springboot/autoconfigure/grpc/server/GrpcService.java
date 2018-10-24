package net.devh.springboot.autoconfigure.grpc.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

import io.grpc.ServerInterceptor;

/**
 * Annotation that marks gRPC services that should be registered with a gRPC server. If
 * spring-boot's auto configuration is used, then the server will be created automatically.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface GrpcService {
    
    // Unused - Should be removed
    @Deprecated
    Class<?> value() default void.class;

    /**
     * A list of {@link ServerInterceptor} that should be applied to only this service. If a bean of the
     * given type exists, it will be used; otherwise a new instance of that class will be created via
     * no-args constructor.
     *
     * <p>
     * <b>Note:</b> These interceptors will be applied after the global interceptors. But the
     * interceptors that were applied last, will be called first.
     * </p>
     *
     * @return A list of ServerInterceptors that should be used.
     */
    Class<? extends ServerInterceptor>[] interceptors() default {};

}

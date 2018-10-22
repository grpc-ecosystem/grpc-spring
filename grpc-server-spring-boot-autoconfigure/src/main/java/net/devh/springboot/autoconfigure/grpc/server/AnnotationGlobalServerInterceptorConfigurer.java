package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automatically find and configure {@link GrpcGlobalServerInterceptor annotated} global
 * {@link ServerInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class AnnotationGlobalServerInterceptorConfigurer implements GlobalServerInterceptorConfigurer {

    @Autowired
    private ApplicationContext context;

    @Override
    public void addServerInterceptors(final GlobalServerInterceptorRegistry registry) {
        final String[] names = this.context.getBeanNamesForAnnotation(GrpcGlobalServerInterceptor.class);
        for (final String name : names) {
            final ServerInterceptor interceptor = this.context.getBean(name, ServerInterceptor.class);
            log.debug("Registering GlobalServerInterceptor: {} ({})", name, interceptor);
            registry.addServerInterceptors(interceptor);
        }
    }

}

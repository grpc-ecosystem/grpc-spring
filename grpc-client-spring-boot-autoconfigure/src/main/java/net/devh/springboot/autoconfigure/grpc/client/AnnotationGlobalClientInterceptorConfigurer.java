package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import io.grpc.ClientInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automatically find and configure {@link GrpcGlobalClientInterceptor annotated} global
 * {@link ClientInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class AnnotationGlobalClientInterceptorConfigurer implements GlobalClientInterceptorConfigurer {

    @Autowired
    private ApplicationContext context;

    @Override
    public void addClientInterceptors(final GlobalClientInterceptorRegistry registry) {
        final String[] names = this.context.getBeanNamesForAnnotation(GrpcGlobalClientInterceptor.class);
        for (final String name : names) {
            final ClientInterceptor interceptor = this.context.getBean(name, ClientInterceptor.class);
            log.debug("Registering GlobalClientInterceptor: {} ({})", name, interceptor);
            registry.addClientInterceptors(interceptor);
        }
    }

}

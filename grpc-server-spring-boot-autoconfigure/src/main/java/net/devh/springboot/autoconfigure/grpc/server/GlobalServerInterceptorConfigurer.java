package net.devh.springboot.autoconfigure.grpc.server;

import io.grpc.ServerInterceptor;

/**
 * This configurer can be used to register new global {@link ServerInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public interface GlobalServerInterceptorConfigurer {

    /**
     * Adds the {@link ServerInterceptor}s that should be registered globally to the given registry.
     *
     * @param registry The registry the interceptors should be added to.
     */
    void addServerInterceptors(GlobalServerInterceptorRegistry registry);

}

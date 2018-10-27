package net.devh.springboot.autoconfigure.grpc.client;

import io.grpc.ClientInterceptor;

/**
 * This configurer can be used to register new global {@link ClientInterceptor}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public interface GlobalClientInterceptorConfigurer {

    /**
     * Adds the {@link ClientInterceptor}s that should be registered globally to the given registry.
     *
     * @param registry The registry the interceptors should be added to.
     */
    void addClientInterceptors(GlobalClientInterceptorRegistry registry);

}

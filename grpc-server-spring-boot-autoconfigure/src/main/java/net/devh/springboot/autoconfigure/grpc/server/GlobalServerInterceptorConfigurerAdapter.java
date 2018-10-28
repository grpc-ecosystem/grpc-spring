package net.devh.springboot.autoconfigure.grpc.server;

/**
 * Abstract class to register server interceptors globally.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 2016/12/6
 * @deprecated Use {@link GlobalServerInterceptorConfigurer} instead.
 */
@Deprecated
public abstract class GlobalServerInterceptorConfigurerAdapter implements GlobalServerInterceptorConfigurer {

    @Override
    public void addServerInterceptors(final GlobalServerInterceptorRegistry registry) {

    }

}

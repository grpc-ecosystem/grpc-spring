package net.devh.springboot.autoconfigure.grpc.client;

/**
 * Abstract class to register client interceptors globally.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 2016/12/6
 * @deprecated Use {@link GlobalClientInterceptorConfigurer} instead.
 */
@Deprecated
public abstract class GlobalClientInterceptorConfigurerAdapter implements GlobalClientInterceptorConfigurer {

    @Override
    public void addClientInterceptors(final GlobalClientInterceptorRegistry registry) {

    }

}

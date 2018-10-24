package net.devh.springboot.autoconfigure.grpc.client;

import io.grpc.LoadBalancer;

/**
 * This channel factory creates new Channels based on {@link GrpcChannelProperties} that can be
 * configured via application properties. This class utilizes connection pooling and thus needs to
 * be {@link #close() closed} after usage.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class AddressChannelFactory extends AbstractChannelFactory {

    public AddressChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        super(properties, loadBalancerFactory, new AddressChannelResolverFactory(properties),
                globalClientInterceptorRegistry);
    }

}

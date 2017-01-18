package net.devh.springboot.autoconfigure.grpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.grpc.Channel;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolver;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class AddressChannelFactory implements GrpcChannelFactory {
    private final GrpcChannelsProperties properties;
    private final LoadBalancer.Factory loadBalancerFactory;
    private final NameResolver.Factory nameResolverFactory;

    private static Map<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    public AddressChannelFactory(GrpcChannelsProperties properties, LoadBalancer.Factory loadBalancerFactory) {
        this.properties = properties;
        this.loadBalancerFactory = loadBalancerFactory;
        this.nameResolverFactory = new AddressChannelResolverFactory(properties);
    }

    @Override
    public Channel createChannel(String name) {
        ManagedChannel channel = channelMap.get(name);
        if (channel == null) {
            ManagedChannel newChannel = ManagedChannelBuilder.forTarget(name)
                    .loadBalancerFactory(loadBalancerFactory)
                    .nameResolverFactory(nameResolverFactory)
                    .usePlaintext(properties.getChannel(name).isPlaintext())
                    .build();
            if (channelMap.putIfAbsent(name, newChannel) == null) {
                channel = newChannel;
            } else {
                channel = channelMap.get(name);
            }
        }
        return channel;
    }
}

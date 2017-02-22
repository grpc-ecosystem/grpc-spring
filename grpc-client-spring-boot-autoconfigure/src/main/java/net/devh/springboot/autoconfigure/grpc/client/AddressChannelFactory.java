package net.devh.springboot.autoconfigure.grpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.grpc.Channel;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.NameResolver;
import io.grpc.netty.NettyChannelBuilder;

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
            synchronized (channelMap) {
                if (channelMap.get(name) == null) {
                    GrpcChannelProperties channelProperties = properties.getChannel(name);
                    channel = NettyChannelBuilder.forTarget(name)
                            .loadBalancerFactory(loadBalancerFactory)
                            .nameResolverFactory(nameResolverFactory)
                            .usePlaintext(channelProperties.isPlaintext())
                            .enableKeepAlive(channelProperties.isEnableKeepAlive(), channelProperties.getKeepAliveDelay(), TimeUnit.SECONDS, channelProperties.getKeepAliveTimeout(), TimeUnit.SECONDS)
                            .build();
                    channelMap.put(name, channel);
                }
            }
        }
        return channel;
    }
}

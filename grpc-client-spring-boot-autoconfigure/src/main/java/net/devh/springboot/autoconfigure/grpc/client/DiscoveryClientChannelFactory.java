package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.grpc.Channel;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class DiscoveryClientChannelFactory implements GrpcChannelFactory {
    private final GrpcChannelsProperties properties;
    private final DiscoveryClient client;
    private final LoadBalancer.Factory loadBalancerFactory;

    private static Map<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    public DiscoveryClientChannelFactory(GrpcChannelsProperties properties, DiscoveryClient client, LoadBalancer.Factory loadBalancerFactory) {
        this.properties = properties;
        this.client = client;
        this.loadBalancerFactory = loadBalancerFactory;
    }

    @Override
    public Channel createChannel(String name) {
        ManagedChannel channel = channelMap.get(name);
        if (channel == null) {
            ManagedChannel newChannel = ManagedChannelBuilder.forTarget(name)
                    .loadBalancerFactory(loadBalancerFactory)
                    .nameResolverFactory(new DiscoveryClientResolverFactory(client))
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

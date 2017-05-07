package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.LoadBalancer;
import io.grpc.netty.NettyChannelBuilder;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class DiscoveryClientChannelFactory implements GrpcChannelFactory {
    private final GrpcChannelsProperties properties;
    private final DiscoveryClient client;
    private final LoadBalancer.Factory loadBalancerFactory;
    private final GlobalClientInterceptorRegistry globalClientInterceptorRegistry;

    public DiscoveryClientChannelFactory(GrpcChannelsProperties properties, DiscoveryClient client, LoadBalancer.Factory loadBalancerFactory,
                                         GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        this.properties = properties;
        this.client = client;
        this.loadBalancerFactory = loadBalancerFactory;
        this.globalClientInterceptorRegistry = globalClientInterceptorRegistry;
    }

    @Override
    public Channel createChannel(String name) {
        return this.createChannel(name, null);
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        GrpcChannelProperties channelProperties = properties.getChannel(name);
        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(name)
                .loadBalancerFactory(loadBalancerFactory)
                .nameResolverFactory(new DiscoveryClientResolverFactory(client))
                .usePlaintext(properties.getChannel(name).isPlaintext());
        if (channelProperties.isEnableKeepAlive()) {
            builder.keepAliveWithoutCalls(channelProperties.isKeepAliveWithoutCalls())
                    .keepAliveTime(channelProperties.getKeepAliveTime(), TimeUnit.SECONDS)
                    .keepAliveTimeout(channelProperties.getKeepAliveTimeout(), TimeUnit.SECONDS);
        }
        Channel channel = builder.build();

        List<ClientInterceptor> globalInterceptorList = globalClientInterceptorRegistry.getClientInterceptors();
        Set<ClientInterceptor> interceptorSet = Sets.newHashSet();
        if (globalInterceptorList != null && !globalInterceptorList.isEmpty()) {
            interceptorSet.addAll(globalInterceptorList);
        }
        if (interceptors != null && !interceptors.isEmpty()) {
            interceptorSet.addAll(interceptors);
        }
        return ClientInterceptors.intercept(channel, Lists.newArrayList(interceptorSet));
    }
}

package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.LoadBalancer;
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
    private final GlobalClientInterceptorRegistry globalClientInterceptorRegistry;

    public AddressChannelFactory(GrpcChannelsProperties properties, LoadBalancer.Factory loadBalancerFactory, GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        this.properties = properties;
        this.loadBalancerFactory = loadBalancerFactory;
        this.nameResolverFactory = new AddressChannelResolverFactory(properties);
        this.globalClientInterceptorRegistry = globalClientInterceptorRegistry;
    }

    @Override
    public Channel createChannel(String name) {
        return this.createChannel(name, null);
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        GrpcChannelProperties channelProperties = properties.getChannel(name);
        Channel channel = NettyChannelBuilder.forTarget(name)
                .loadBalancerFactory(loadBalancerFactory)
                .nameResolverFactory(nameResolverFactory)
                .usePlaintext(channelProperties.isPlaintext())
                .enableKeepAlive(channelProperties.isEnableKeepAlive(), channelProperties.getKeepAliveDelay(), TimeUnit.SECONDS, channelProperties.getKeepAliveTimeout(), TimeUnit.SECONDS)
                .build();
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

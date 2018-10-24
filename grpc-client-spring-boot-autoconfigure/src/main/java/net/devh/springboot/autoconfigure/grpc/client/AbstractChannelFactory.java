package net.devh.springboot.autoconfigure.grpc.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;

import com.google.common.collect.Lists;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.NameResolver;
import io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * This abstract channel factory contains some shared code for other {@link GrpcChannelFactory}s.
 * This class utilizes connection pooling and thus needs to be {@link #close() closed} after usage.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @since 5/17/16
 */
@Slf4j
public abstract class AbstractChannelFactory implements GrpcChannelFactory {

    private final GrpcChannelsProperties properties;
    private final LoadBalancer.Factory loadBalancerFactory;
    private final NameResolver.Factory nameResolverFactory;
    private final GlobalClientInterceptorRegistry globalClientInterceptorRegistry;
    /**
     * According to <a href="https://groups.google.com/forum/#!topic/grpc-io/-jA_JCiugM8">Thread safety
     * in Grpc java clients</a>: {@link ManagedChannel}s should be reused to allow connection reuse.
     */
    @GuardedBy("this")
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private boolean shutdown = false;

    public AbstractChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory,
            final NameResolver.Factory nameResolverFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        this.properties = properties;
        this.loadBalancerFactory = loadBalancerFactory;
        this.nameResolverFactory = nameResolverFactory;
        this.globalClientInterceptorRegistry = globalClientInterceptorRegistry;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends AbstractChannelFactory> AbstractChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory,
            final Function<T, NameResolver.Factory> nameResolverFactoryCreator,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        this.properties = properties;
        this.loadBalancerFactory = loadBalancerFactory;
        this.nameResolverFactory = nameResolverFactoryCreator.apply((T) this);
        this.globalClientInterceptorRegistry = globalClientInterceptorRegistry;
    }

    @Override
    public Channel createChannel(final String name) {
        return createChannel(name, Collections.emptyList());
    }

    @Override
    public Channel createChannel(final String name, final List<ClientInterceptor> interceptors) {
        final Channel channel;
        synchronized (this) {
            if (this.shutdown) {
                throw new IllegalStateException("GrpcChannelFactory is already closed!");
            }
            channel = this.channels.computeIfAbsent(name, this::newManagedChannel);
        }

        final List<ClientInterceptor> globalInterceptorList =
                this.globalClientInterceptorRegistry.getClientInterceptors();
        final Collection<ClientInterceptor> interceptorSet = Lists.newArrayList();
        if (!globalInterceptorList.isEmpty()) {
            interceptorSet.addAll(globalInterceptorList);
        }
        if (!interceptors.isEmpty()) {
            interceptorSet.addAll(interceptors);
        }
        return ClientInterceptors.intercept(channel, Lists.newArrayList(interceptorSet));
    }

    private ManagedChannel newManagedChannel(final String name) {
        final GrpcChannelProperties channelProperties = this.properties.getChannel(name);
        final NettyChannelBuilder builder = NettyChannelBuilder.forTarget(name)
                .loadBalancerFactory(this.loadBalancerFactory)
                .nameResolverFactory(this.nameResolverFactory);
        builder.negotiationType(channelProperties.getNegotiationType());
        if (channelProperties.isEnableKeepAlive()) {
            builder.keepAliveWithoutCalls(channelProperties.isKeepAliveWithoutCalls())
                    .keepAliveTime(channelProperties.getKeepAliveTime(), TimeUnit.SECONDS)
                    .keepAliveTimeout(channelProperties.getKeepAliveTimeout(), TimeUnit.SECONDS);
        }
        if (channelProperties.getMaxInboundMessageSize() >= 0) {
            builder.maxInboundMessageSize(channelProperties.getMaxInboundMessageSize());
        } else if (channelProperties.getMaxInboundMessageSize() == -1) {
            builder.maxInboundMessageSize(Integer.MAX_VALUE);
        }
        if (channelProperties.isFullStreamDecompression()) {
            builder.enableFullStreamDecompression();
        }
        return builder.build();
    }

    /**
     * Closes this channel factory and the channels created by this instance. The shutdown happens in
     * two phases, first an orderly shutdown is initiated on all channels and then the method waits for
     * all channels to terminate.
     */
    @Override
    @PreDestroy
    public synchronized void close() throws InterruptedException {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        for (final ManagedChannel channel : this.channels.values()) {
            channel.shutdown();
        }
        for (final ManagedChannel channel : this.channels.values()) {
            int i = 0;
            do {
                log.debug("Awaiting channel shutdown: {} ({}s)", channel, i++);
            } while (!channel.awaitTermination(1, TimeUnit.SECONDS));
        }
        final int channelCount = this.channels.size();
        this.channels.clear();
        log.debug("GrpcCannelFactory closed (including {} channels)", channelCount);
    }

}

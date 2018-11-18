/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.springboot.autoconfigure.grpc.client;

import java.util.List;

import javax.annotation.PreDestroy;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.context.event.EventListener;

import com.google.common.collect.Lists;

import io.grpc.LoadBalancer;

/**
 * This channel factory creates new Channels using a {@link DiscoveryClient service discovery}. This class utilizes
 * connection pooling and thus needs to be {@link #close() closed} after usage.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class DiscoveryClientChannelFactory extends AbstractNettyChannelFactory {

    private final HeartbeatMonitor monitor = new HeartbeatMonitor();
    private final List<DiscoveryClientNameResolver> discoveryClientNameResolvers = Lists.newArrayList();

    /**
     * Creates a new DiscoveryClientChannelFactory that will use the discovery client to resolve the grpc server
     * address.
     *
     * @param properties The properties for the channels to create.
     * @param loadBalancerFactory The load balancer factory to use.
     * @param client The discovery client to use.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     * @param channelConfigurers The channel configurers to use. Can be empty.
     */
    public DiscoveryClientChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory, final DiscoveryClient client,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        <DiscoveryClientChannelFactory>super(properties, loadBalancerFactory,
                thiz -> new DiscoveryClientResolverFactory(client, thiz),
                globalClientInterceptorRegistry, channelConfigurers);
    }

    /**
     * Registers the given name resolver. Registered name resolvers will be automatically refreshed during a
     * {@link HeartbeatEvent}.
     *
     * @param discoveryClientNameResolver The the name resolver to register.
     * @see #heartbeat(HeartbeatEvent)
     */
    public void addDiscoveryClientNameResolver(final DiscoveryClientNameResolver discoveryClientNameResolver) {
        this.discoveryClientNameResolvers.add(discoveryClientNameResolver);
    }

    /**
     * Triggers a refresh of the registered name resolvers.
     *
     * @param event The event that triggered the update.
     */
    @EventListener(HeartbeatEvent.class)
    public void heartbeat(final HeartbeatEvent event) {
        if (this.monitor.update(event.getValue())) {
            for (final DiscoveryClientNameResolver discoveryClientNameResolver : this.discoveryClientNameResolvers) {
                discoveryClientNameResolver.refresh();
            }
        }
    }

    /**
     * Cleans up the name resolvers.
     */
    @PreDestroy
    public void destroy() {
        this.discoveryClientNameResolvers.clear();
    }

}

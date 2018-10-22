package net.devh.springboot.autoconfigure.grpc.client;

import java.util.List;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.context.event.EventListener;

import com.google.common.collect.Lists;

import io.grpc.LoadBalancer;

/**
 * This channel factory creates new Channels using a {@link DiscoveryClient service discovery}. This
 * class utilizes connection pooling and thus needs to be {@link #close() closed} after usage.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class DiscoveryClientChannelFactory extends AbstractChannelFactory {

    private final HeartbeatMonitor monitor = new HeartbeatMonitor();
    private final List<DiscoveryClientNameResolver> discoveryClientNameResolvers = Lists.newArrayList();

    public DiscoveryClientChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory,
            final DiscoveryClient client,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        <DiscoveryClientChannelFactory>super(properties,
                loadBalancerFactory,
                thiz -> new DiscoveryClientResolverFactory(client, thiz),
                globalClientInterceptorRegistry);
    }

    public void addDiscoveryClientNameResolver(final DiscoveryClientNameResolver discoveryClientNameResolver) {
        this.discoveryClientNameResolvers.add(discoveryClientNameResolver);
    }

    @EventListener(HeartbeatEvent.class)
    public void heartbeat(final HeartbeatEvent event) {
        if (this.monitor.update(event.getValue())) {
            for (final DiscoveryClientNameResolver discoveryClientNameResolver : this.discoveryClientNameResolvers) {
                discoveryClientNameResolver.refresh();
            }
        }
    }

}

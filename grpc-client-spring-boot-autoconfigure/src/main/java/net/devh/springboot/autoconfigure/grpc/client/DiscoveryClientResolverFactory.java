package net.devh.springboot.autoconfigure.grpc.client;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class DiscoveryClientResolverFactory extends NameResolverProvider {
    private final DiscoveryClient client;
    private DiscoveryClientChannelFactory discoveryClientChannelFactory;

    public DiscoveryClientResolverFactory(DiscoveryClient client, DiscoveryClientChannelFactory discoveryClientChannelFactory) {
        this.client = client;
        this.discoveryClientChannelFactory = discoveryClientChannelFactory;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        DiscoveryClientNameResolver discoveryClientNameResolver = new DiscoveryClientNameResolver(targetUri.toString(), client, params, GrpcUtil.TIMER_SERVICE, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
        discoveryClientChannelFactory.addDiscoveryClientNameResolver(discoveryClientNameResolver);
        return discoveryClientNameResolver;
    }

    @Override
    public String getDefaultScheme() {
        return "discoveryClient";
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }
}

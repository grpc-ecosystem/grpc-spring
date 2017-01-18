package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.URI;

import javax.annotation.Nullable;

import io.grpc.Attributes;
import io.grpc.NameResolver;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class DiscoveryClientResolverFactory extends NameResolver.Factory {
    private final DiscoveryClient client;

    public DiscoveryClientResolverFactory(DiscoveryClient client) {
        this.client = client;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        return new DiscoveryClientNameResolver(targetUri.toString(), client, params);
    }

    @Override
    public String getDefaultScheme() {
        return "spring";
    }
}

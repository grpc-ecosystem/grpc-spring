package net.devh.springboot.autoconfigure.grpc.client;

import java.net.URI;

import javax.annotation.Nullable;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class AddressChannelResolverFactory extends NameResolverProvider {

    private final GrpcChannelsProperties properties;

    public AddressChannelResolverFactory(GrpcChannelsProperties properties) {
        this.properties = properties;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        return new AddressChannelNameResolver(targetUri.toString(), properties.getChannel(targetUri.toString()), params, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
    }

    @Override
    public String getDefaultScheme() {
        return "address";
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

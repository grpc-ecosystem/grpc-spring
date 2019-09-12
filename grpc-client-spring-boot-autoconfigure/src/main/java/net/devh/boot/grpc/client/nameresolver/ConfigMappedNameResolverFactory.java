/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.client.nameresolver;

import static java.util.Objects.requireNonNull;

import java.net.URI;

import javax.annotation.Nullable;

import io.grpc.NameResolver;
import io.grpc.NameResolverRegistry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;

/**
 * A {@link NameResolver} factory that uses the the properties to rewrite the target uri.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class ConfigMappedNameResolverFactory extends NameResolver.Factory {

    private final GrpcChannelsProperties config;
    private final NameResolver.Factory delegate;

    /**
     * Creates a new ConfigMappedNameResolverFactory with the given config that resolves the remapped target uri using
     * the grpc's registered name resolvers.
     *
     * @param config The config used to remap the target uri.
     */
    public ConfigMappedNameResolverFactory(final GrpcChannelsProperties config) {
        this(config, NameResolverRegistry.getDefaultRegistry());
    }

    /**
     * Creates a new ConfigMappedNameResolverFactory with the given config that resolves the remapped target uri using
     * the grpc's registered name resolvers.
     *
     * @param config The config used to remap the target uri.
     * @param registry The registry to use as {@link io.grpc.NameResolver.Factory NameResolver.Factory} delegate.
     */
    public ConfigMappedNameResolverFactory(final GrpcChannelsProperties config, NameResolverRegistry registry) {
        this(config, registry.asFactory());
    }

    /**
     * Creates a new ConfigMappedNameResolverFactory with the given config that resolves the remapped target uri using
     * the given delegate.
     *
     * @param config The config used to remap the target uri.
     * @param delegate The delegate used to resolve the remapped target uri.
     */
    public ConfigMappedNameResolverFactory(final GrpcChannelsProperties config, final NameResolver.Factory delegate) {
        this.config = requireNonNull(config, "config");
        this.delegate = requireNonNull(delegate, "delegate");
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(final URI targetUri, final NameResolver.Args args) {
        final String clientName = targetUri.toString();
        final GrpcChannelProperties clientConfig = this.config.getChannel(clientName);
        URI remappedUri = clientConfig.getAddress();
        if (remappedUri == null) {
            remappedUri = URI.create(clientName);
        }
        log.debug("Remapping target URI for {} to {} via {}", clientName, remappedUri, this.delegate);
        NameResolver resolver = this.delegate.newNameResolver(remappedUri, args);
        if (resolver != null) {
            return resolver;
        }
        remappedUri = URI.create(getDefaultSchemeInternal() + ":/" + remappedUri.toString());
        log.debug("Remapping target URI (with default scheme) for {} to {} via {}",
                clientName, remappedUri, this.delegate);
        return this.delegate.newNameResolver(remappedUri, args);
    }

    @Override
    public String getDefaultScheme() {
        // The config does not use schemes at all
        return "";
    }

    private String getDefaultSchemeInternal() {
        String configured = this.config.getDefaultScheme();
        if (configured != null) {
            return configured;
        }
        return this.delegate.getDefaultScheme();
    }

    @Override
    public String toString() {
        return "ConfigMappedNameResolverFactory [config=" + this.config + ", delegate=" + this.delegate + "]";
    }

}

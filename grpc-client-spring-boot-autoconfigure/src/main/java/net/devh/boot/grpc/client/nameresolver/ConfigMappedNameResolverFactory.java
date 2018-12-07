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

package net.devh.boot.grpc.client.nameresolver;

import static java.util.Objects.requireNonNull;

import java.net.URI;

import javax.annotation.Nullable;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;

/**
 * A {@link NameResolver} factory that uses the the properties to rewrite the target uri.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ConfigMappedNameResolverFactory extends NameResolver.Factory {

    private final GrpcChannelsProperties config;
    private final NameResolver.Factory delegate;

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
    public NameResolver newNameResolver(final URI targetUri, final Attributes params) {
        final String clientName = targetUri.getAuthority();
        final GrpcChannelProperties clientConfig = this.config.getChannel(clientName);
        final Attributes extendedParas = params.toBuilder()
                .set(NameResolverConstants.PARAMS_CLIENT_NAME, clientName)
                .set(NameResolverConstants.PARAMS_CLIENT_CONFIG, clientConfig)
                .build();
        return this.delegate.newNameResolver(clientConfig.getAddress(), extendedParas);
    }

    @Override
    public String getDefaultScheme() {
        return "";
    }

}

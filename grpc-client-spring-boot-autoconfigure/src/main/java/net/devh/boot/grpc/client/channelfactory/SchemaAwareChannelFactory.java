/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.client.channelfactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;

/**
 * Creates a schema aware channel factory. The actual {@link GrpcChannelFactory} is chosen based on the scheme of the
 * address associated to the requested channel name.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class SchemaAwareChannelFactory implements GrpcChannelFactory {

    private final GrpcChannelsProperties properties;
    private final Map<String, GrpcChannelFactory> factories;
    private final GrpcChannelFactory fallback;

    /**
     * Creates a new SchemaAwareChannelFactory.
     *
     * @param properties The properties to lookup the addresses.
     * @param fallback The fallback factory to use if the scheme does not match a specific factory.
     */
    public SchemaAwareChannelFactory(
            final GrpcChannelsProperties properties,
            final GrpcChannelFactory fallback) {

        this(properties, new HashMap<>(), fallback);
    }

    /**
     * Creates a new SchemaAwareChannelFactory.
     *
     * @param properties The properties to lookup the addresses.
     * @param factories The factories by their associated scheme.
     * @param fallback The fallback factory to use if the scheme does not match a specific factory.
     */
    public SchemaAwareChannelFactory(
            final GrpcChannelsProperties properties,
            final Map<String, GrpcChannelFactory> factories,
            final GrpcChannelFactory fallback) {

        this.properties = properties;
        this.factories = new HashMap<>(factories);
        this.fallback = fallback;
    }

    @Override
    public Channel createChannel(
            final String name,
            final List<ClientInterceptor> interceptors,
            final boolean sortInterceptors) {

        final URI address = this.properties.getChannel(name).getAddress();
        final String scheme = address == null ? null : address.getScheme();
        return this.factories.getOrDefault(scheme, this.fallback)
                .createChannel(name, interceptors, sortInterceptors);
    }

    public SchemaAwareChannelFactory put(final String scheme, final GrpcChannelFactory factory) {
        this.factories.put(scheme, factory);
        return this;
    }

    @Override
    public void close() {
        for (final GrpcChannelFactory factory : this.factories.values()) {
            factory.close();
        }
        this.fallback.close();
    }

}

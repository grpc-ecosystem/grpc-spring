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

package net.devh.boot.grpc.client.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A container for named channel properties. Each channel has its own configuration. If you try to get a channel that
 * does not have a configuration yet, it will be created. If something is not configured in the channel properties, it
 * will be copied from the global config during the first retrieval. If some property is configured in neither the
 * channel properties nor the global properties then a default value will be used.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @since 5/17/16
 */
@ToString
@EqualsAndHashCode
@ConfigurationProperties("grpc")
public class GrpcChannelsProperties {

    /**
     * The key that will be used for the {@code GLOBAL} properties.
     */
    public static final String GLOBAL_PROPERTIES_KEY = "GLOBAL";

    private final Map<String, GrpcChannelProperties> client = new ConcurrentHashMap<>();;

    /**
     * Gets the configuration mapping for each client.
     *
     * @return The client configuration mappings.
     */
    public final Map<String, GrpcChannelProperties> getClient() {
        return this.client;
    }

    /**
     * Gets the properties for the given channel. If the properties for the specified channel name do not yet exist,
     * they are created automatically. Before the instance is returned, the unset values are filled with values from the
     * global properties.
     *
     * @param name The name of the channel to get the properties for.
     * @return The properties for the given channel name.
     */
    public GrpcChannelProperties getChannel(final String name) {
        final GrpcChannelProperties properties = getRawChannel(name);
        properties.copyDefaultsFrom(getGlobalChannel());
        return properties;
    }

    /**
     * Gets the global channel properties. Global properties are used, if the channel properties don't overwrite them.
     * If neither the global nor the per client properties are set then default values will be used.
     *
     * @return The global channel properties.
     */
    public final GrpcChannelProperties getGlobalChannel() {
        // This cannot be moved to its own field,
        // as Spring replaces the instance in the map and inconsistencies would occur.
        return getRawChannel(GLOBAL_PROPERTIES_KEY);
    }

    /**
     * Gets or creates the channel properties for the given client.
     *
     * @param name The name of the channel to get the properties for.
     * @return The properties for the given channel name.
     */
    private GrpcChannelProperties getRawChannel(final String name) {
        return this.client.computeIfAbsent(name, key -> new GrpcChannelProperties());
    }

}

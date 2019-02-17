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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.google.common.collect.Maps;

import lombok.Data;

/**
 * The container for named channel properties.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Data
@ConfigurationProperties("grpc")
@SuppressWarnings("javadoc")
public class GrpcChannelsProperties {

    /**
     * The configuration mapping for each client.
     *
     * @param client The client mappings to use.
     * @return The client mappings to use.
     */
    @NestedConfigurationProperty
    private final Map<String, GrpcChannelProperties> client = Maps.newHashMap();

    /**
     * Gets the properties for the given channel. This will return an instance with default values, if the channel does
     * not have any configuration.
     *
     * @param name The name of the channel to get the properties for.
     * @return The properties for the given channel name or an instance with default value, if it does not exist.
     */
    public GrpcChannelProperties getChannel(final String name) {
        return this.client.getOrDefault(name, GrpcChannelProperties.DEFAULT);
    }

}

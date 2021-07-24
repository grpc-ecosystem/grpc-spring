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

import java.util.Collections;
import java.util.List;

import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;

/**
 * This channel factory creates and manages in-process {@link GrpcChannelFactory}s.
 *
 * <p>
 * This class utilizes connection pooling and thus needs to be {@link #close() closed} after usage.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class InProcessChannelFactory extends AbstractChannelFactory<InProcessChannelBuilder> {

    /**
     * Creates a new InProcessChannelFactory with the given properties.
     *
     * @param properties The properties for the channels to create.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     */
    public InProcessChannelFactory(final GrpcChannelsProperties properties,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        this(properties, globalClientInterceptorRegistry, Collections.emptyList());
    }

    /**
     * Creates a new InProcessChannelFactory with the given properties.
     *
     * @param properties The properties for the channels to create.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     * @param channelConfigurers The channel configurers to use. Can be empty.
     */
    public InProcessChannelFactory(final GrpcChannelsProperties properties,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        super(properties, globalClientInterceptorRegistry, channelConfigurers);
    }

    @Override
    protected InProcessChannelBuilder newChannelBuilder(final String name) {
        log.debug("Creating new channel: {}", name);
        return InProcessChannelBuilder.forName(name);
    }

    @Override
    protected void configureSecurity(final InProcessChannelBuilder builder, final String name) {
        // No need to configure security as we are in process only.
        // There is also no need to throw exceptions if transport security is configured.
    }

}

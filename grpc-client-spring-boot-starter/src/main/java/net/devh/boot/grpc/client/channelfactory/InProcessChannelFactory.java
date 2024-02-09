/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

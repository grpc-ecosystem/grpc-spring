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

package net.devh.boot.grpc.server.serverfactory;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;

import io.grpc.inprocess.InProcessServerBuilder;
import net.devh.boot.grpc.server.config.GrpcServerProperties;

/**
 * Factory for in process grpc servers.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class InProcessGrpcServerFactory extends AbstractGrpcServerFactory<InProcessServerBuilder> {

    private final String name;

    /**
     * Creates a new in process server factory with the given properties.
     *
     * @param properties The properties used to configure the server.
     */
    public InProcessGrpcServerFactory(final GrpcServerProperties properties) {
        this(properties.getInProcessName(), properties);
    }

    /**
     * Creates a new in process server factory with the given properties.
     *
     * @param properties The properties used to configure the server.
     * @param serverConfigurers The server configurers to use. Can be empty.
     */
    public InProcessGrpcServerFactory(final GrpcServerProperties properties,
            final List<GrpcServerConfigurer> serverConfigurers) {
        this(properties.getInProcessName(), properties, serverConfigurers);
    }

    /**
     * Creates a new in process server factory with the given properties.
     *
     * @param name The name of the in process server.
     * @param properties The properties used to configure the server.
     */
    public InProcessGrpcServerFactory(final String name, final GrpcServerProperties properties) {
        this(name, properties, Collections.emptyList());
    }

    /**
     * Creates a new in process server factory with the given properties.
     *
     * @param name The name of the in process server.
     * @param properties The properties used to configure the server.
     * @param serverConfigurers The server configurers to use. Can be empty.
     */
    public InProcessGrpcServerFactory(final String name, final GrpcServerProperties properties,
            final List<GrpcServerConfigurer> serverConfigurers) {
        super(properties, serverConfigurers);
        this.name = requireNonNull(name, "name");
    }

    @Override
    protected InProcessServerBuilder newServerBuilder() {
        return InProcessServerBuilder.forName(this.name);
    }

    @Override
    protected void configureSecurity(final InProcessServerBuilder builder) {
        // No need to configure security as we are in process only.
        // There is also no need to throw exceptions if transport security is configured.
    }

    @Override
    public String getAddress() {
        return "in-process:" + this.name;
    }

    @Override
    public int getPort() {
        return -1;
    }

}

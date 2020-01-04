/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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
    public String getAddress() {
        return "in-process:" + this.name;
    }

    @Override
    public int getPort() {
        return -1;
    }

}

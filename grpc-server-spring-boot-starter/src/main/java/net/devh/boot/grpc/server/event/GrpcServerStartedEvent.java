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

package net.devh.boot.grpc.server.event;

import static java.util.Objects.requireNonNull;

import java.time.Clock;

import io.grpc.Server;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;

/**
 * This event will be fired after the server has been started.
 *
 * @see Server#start()
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class GrpcServerStartedEvent extends GrpcServerLifecycleEvent {

    private static final long serialVersionUID = 1L;

    private final String address;
    private final int port;

    /**
     * Creates a new GrpcServerStartedEvent.
     *
     * @param lifecyle The lifecycle that caused this event.
     * @param clock The clock used to determine the timestamp.
     * @param server The server related to this event.
     * @param address The address the server is bound to.
     * @param port The port the server is bound to or {@code -1} if it isn't bound to a particular port.
     */
    public GrpcServerStartedEvent(
            final GrpcServerLifecycle lifecyle,
            final Clock clock,
            final Server server,
            final String address, final int port) {

        super(lifecyle, clock, server);
        this.address = requireNonNull(address, "address");
        this.port = port;
    }

    /**
     * Creates a new GrpcServerStartedEvent.
     *
     * @param lifecyle The lifecycle that caused this event.
     * @param server The server related to this event.
     * @param address The address the server is bound to.
     * @param port The port the server is bound to or {@code -1} if it isn't bound to a particular port.
     */
    public GrpcServerStartedEvent(
            final GrpcServerLifecycle lifecyle,
            final Server server,
            final String address, final int port) {

        super(lifecyle, server);
        this.address = requireNonNull(address, "address");
        this.port = port;
    }

    /**
     * Gets the address the server server was started with.
     *
     * @return The address to use.
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Gets the main port the server uses.
     *
     * @return The main port of the server. {@code -1} indicates that the server isn't bound to a particular port.
     */
    public int getPort() {
        return this.port;
    }

}

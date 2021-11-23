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

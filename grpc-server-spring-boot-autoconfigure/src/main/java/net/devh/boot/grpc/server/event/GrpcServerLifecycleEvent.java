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

import org.springframework.context.ApplicationEvent;

import io.grpc.Server;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;

/**
 * The base event for {@link GrpcServerLifecycle}.
 */
public abstract class GrpcServerLifecycleEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final Server server;

    /**
     * Creates a new GrpcServerLifecycleEvent.
     *
     * @param lifecyle The lifecycle that caused this event.
     * @param clock The clock used to determine the timestamp.
     * @param server The server related to this event.
     */
    protected GrpcServerLifecycleEvent(final GrpcServerLifecycle lifecyle, final Clock clock, final Server server) {
        super(lifecyle, clock);
        this.server = requireNonNull(server, "server");
    }

    /**
     * Creates a new GrpcServerLifecycleEvent.
     *
     * @param lifecyle The lifecycle that caused this event.
     * @param server The server related to this event.
     */
    protected GrpcServerLifecycleEvent(final GrpcServerLifecycle lifecyle, final Server server) {
        super(lifecyle);
        this.server = requireNonNull(server, "server");
    }

    /**
     * Gets the server related to this event.
     *
     * @return The server instance.
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Gets the lifecycle instance that triggered this event.
     */
    @Override
    public GrpcServerLifecycle getSource() {
        return (GrpcServerLifecycle) super.getSource();
    }

}

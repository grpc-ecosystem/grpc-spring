/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.client.nameresolver;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;

/**
 * Helper class with some constants that are used by {@link NameResolver}s or their factories.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class NameResolverConstants {

    /**
     * The name of the client, that was used to create the connection.
     */
    @Deprecated
    public static final Attributes.Key<String> PARAMS_CLIENT_NAME =
            Attributes.Key.create("params-client-name");
    /**
     * The configuration that is associated with the client.
     */
    @Deprecated
    public static final Attributes.Key<GrpcChannelProperties> PARAMS_CLIENT_CONFIG =
            Attributes.Key.create("params-client-config");
    /**
     * The default port used if no specific port is configured.
     */
    public static final int DEFAULT_PORT = 9090;

    private NameResolverConstants() {}

}

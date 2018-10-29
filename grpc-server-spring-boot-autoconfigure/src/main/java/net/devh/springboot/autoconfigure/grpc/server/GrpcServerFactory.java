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

package net.devh.springboot.autoconfigure.grpc.server;

import io.grpc.Server;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

/**
 * A factory that can be used to create grpc servers.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public interface GrpcServerFactory {

    /**
     * Creates a new grpc server with the stored options. The entire lifecycle management of the server should be
     * managed by the calling class. This includes starting and stopping the server.
     *
     * @return The newly created grpc server.
     */
    Server createServer();

    /**
     * Gets the IP address the created server will be bound to.
     *
     * @return The IP address the server will be bound to.
     */
    String getAddress();

    /**
     * Gets the local port the created server will use to listen to requests.
     *
     * @return Gets the local port the server will use.
     */
    int getPort();

    /**
     * Adds the given grpc service definition to this factory. The created server will serve the services described by
     * these definitions.
     *
     * <p>
     * <b>Note:</b> Adding a service does not effect servers that have already been created.
     * </p>
     *
     * @param service The service to add to the grpc server.
     */
    void addService(GrpcServiceDefinition service);

    /**
     * Adds the given grpc codec definition to this factory. The created server will use the codec described by these
     * definitions.
     *
     * <p>
     * <b>Note:</b> Adding a codec does not effect servers that have already been created.
     * </p>
     *
     * @param codec The codec to add to the grpc server.
     */
    void addCodec(GrpcCodecDefinition codec);

    /**
     * Destroys this factory. This does not destroy or shutdown any server that was created using this factory.
     */
    void destroy();

}

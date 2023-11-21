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

import io.grpc.Server;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;

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

}

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

package net.devh.boot.grpc.server.service;

import java.util.Collection;

import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;

/**
 * An interface for a bean that will be used to find grpc services and codecs. These will then be provided to the
 * {@link GrpcServerFactory} which then uses them to configure the server.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@FunctionalInterface
public interface GrpcServiceDiscoverer {

    /**
     * Find the grpc services that should provided by the server.
     *
     * @return The grpc services that should be provided. Never null.
     */
    Collection<GrpcServiceDefinition> findGrpcServices();

}

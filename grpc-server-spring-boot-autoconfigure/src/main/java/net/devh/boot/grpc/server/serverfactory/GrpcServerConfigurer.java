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

import java.util.Objects;
import java.util.function.Consumer;

import io.grpc.ServerBuilder;

/**
 * A configurer for {@link ServerBuilder}s which can be used by {@link GrpcServerFactory} to customize the created
 * server.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@FunctionalInterface
public interface GrpcServerConfigurer extends Consumer<ServerBuilder<?>> {

    @Override
    default GrpcServerConfigurer andThen(final Consumer<? super ServerBuilder<?>> after) {
        Objects.requireNonNull(after);
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

}

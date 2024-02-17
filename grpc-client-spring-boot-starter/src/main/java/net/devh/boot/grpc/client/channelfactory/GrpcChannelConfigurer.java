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

package net.devh.boot.grpc.client.channelfactory;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;

import io.grpc.ManagedChannelBuilder;

/**
 * A configurer for {@link ManagedChannelBuilder}s which can be used by {@link GrpcChannelFactory} to customize the
 * created channels.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@FunctionalInterface
public interface GrpcChannelConfigurer extends BiConsumer<ManagedChannelBuilder<?>, String> {

    @Override
    default GrpcChannelConfigurer andThen(final BiConsumer<? super ManagedChannelBuilder<?>, ? super String> after) {
        requireNonNull(after, "after");
        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }

}

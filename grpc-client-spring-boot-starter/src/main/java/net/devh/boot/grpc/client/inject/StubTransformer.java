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

package net.devh.boot.grpc.client.inject;

import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;

/**
 * A stub transformer will be used by the {@link GrpcClientBeanPostProcessor} to configure the stubs before they are
 * assigned to their fields. Implementations should only call the {@code AbstractStub#with...} methods on the given
 * stubs and return that result. Implementations should not use this transformer to replace the stub with a unrelated
 * other instance.
 *
 * <p>
 * <b>Note:</b> StubTransformer will only transform {@link AbstractStub}s and NOT {@link Channel}s. To configure
 * channels use the {@link GrpcChannelFactory}.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@FunctionalInterface
public interface StubTransformer {

    /**
     * Transform the given stub using {@code AbstractStub#with...} methods.
     *
     * @param name The name that was used to create the stub.
     * @param stub The stub that should be transformed.
     * @return The transformed stub.
     */
    AbstractStub<?> transform(String name, AbstractStub<?> stub);

}

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

package net.devh.boot.grpc.client.stubfactory;

import org.springframework.beans.BeanInstantiationException;

import io.grpc.Channel;
import io.grpc.stub.AbstractStub;

/**
 * A factory for gRPC stubs. This is an extension mechanism for supporting different types of gRPC compiled stubs in
 * addition to the standard Java compiled gRPC.
 *
 * Spring beans implementing this type will be picked up automatically and added to the list of supported types.
 */
public interface StubFactory {

    /**
     * Creates a stub of the given type.
     *
     * @param stubType The type of the stub to create.
     * @param channel The channel used to create the stub.
     * @return The newly created stub.
     *
     * @throws BeanInstantiationException If the stub couldn't be created.
     */
    AbstractStub<?> createStub(Class<? extends AbstractStub<?>> stubType, Channel channel);

    /**
     * Used to resolve a factory that matches the particular stub type.
     * 
     * @param stubType The type of the stub that needs to be created.
     * @return True if this particular factory is capable of creating instances of this stub type. False otherwise.
     */
    boolean isApplicable(Class<? extends AbstractStub<?>> stubType);
}

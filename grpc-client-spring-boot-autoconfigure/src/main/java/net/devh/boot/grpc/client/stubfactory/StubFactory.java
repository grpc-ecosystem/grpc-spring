/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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
    AbstractStub<?> createStub(final Class<? extends AbstractStub<?>> stubType, final Channel channel);

    /**
     * Used to resolve a factory that matches the particular stub type.
     * 
     * @param stubType The type of the stub that needs to be created.
     * @return True if this particular factory is capable of creating instances of this stub type. False otherwise.
     */
    boolean isApplicable(Class<? extends AbstractStub<?>> stubType);
}

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.BeanInstantiationException;

import io.grpc.Channel;
import io.grpc.stub.AbstractStub;

/**
 * A factory for creating stubs provided by standard grpc Java library. This is an abstract super-type that can be
 * extended to support the different provided types.
 */
public abstract class StandardJavaGrpcStubFactory implements StubFactory {

    /**
     * Creates a stub of the given type.
     *
     * @param stubType The type of the stub to create.
     * @param channel The channel used to create the stub.
     * @return The newly created stub.
     *
     * @throws BeanInstantiationException If the stub couldn't be created.
     */
    @Override
    public AbstractStub<?> createStub(final Class<? extends AbstractStub<?>> stubType, final Channel channel) {
        try {
            // First try the public static factory method
            final String methodName = getFactoryMethodName();
            final Class<?> enclosingClass = stubType.getEnclosingClass();
            final Method factoryMethod = enclosingClass.getMethod(methodName, Channel.class);
            return stubType.cast(factoryMethod.invoke(null, channel));
        } catch (final Exception e) {
            try {
                // Use the private constructor as backup
                final Constructor<? extends AbstractStub<?>> constructor =
                        stubType.getDeclaredConstructor(Channel.class);
                constructor.setAccessible(true);
                return constructor.newInstance(channel);
            } catch (final Exception e1) {
                e.addSuppressed(e1);
            }
            throw new BeanInstantiationException(stubType, "Failed to create gRPC client", e);
        }
    }

    /**
     * Derives the name of the factory method from the given stub type.
     *
     * @return The name of the factory method.
     */
    protected abstract String getFactoryMethodName();
}

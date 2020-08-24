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
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import org.springframework.beans.BeanInstantiationException;

import io.grpc.Channel;
import io.grpc.stub.AbstractStub;

/**
 * The StubFactory which tries to find a suitable factory method or constructor as a last resort. This factory will
 * always be the last one that is attempted.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class FallbackStubFactory implements StubFactory {

    @Override
    public boolean isApplicable(final Class<? extends AbstractStub<?>> stubType) {
        return true;
    }

    @Override
    public AbstractStub<?> createStub(final Class<? extends AbstractStub<?>> stubType, final Channel channel) {
        try {
            // Search for public static *Grpc#new*Stub(Channel)
            final Class<?> declaringClass = stubType.getDeclaringClass();
            if (declaringClass != null) {
                for (final Method method : declaringClass.getMethods()) {
                    final String name = method.getName();
                    final int modifiers = method.getModifiers();
                    final Parameter[] parameters = method.getParameters();
                    if (name.startsWith("new") && name.endsWith("Stub")
                            && Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
                            && method.getReturnType().isAssignableFrom(stubType)
                            && parameters.length == 1
                            && Channel.class.equals(parameters[0].getType())) {
                        return AbstractStub.class.cast(method.invoke(null, channel));
                    }
                }
            }

            // Search for a public constructor *Stub(Channel)
            final Constructor<? extends AbstractStub<?>> constructor = stubType.getConstructor(Channel.class);
            return constructor.newInstance(channel);

        } catch (final Exception e) {
            throw new BeanInstantiationException(stubType, "Failed to create gRPC client via FallbackStubFactory", e);
        }
    }

}

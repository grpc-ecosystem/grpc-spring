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

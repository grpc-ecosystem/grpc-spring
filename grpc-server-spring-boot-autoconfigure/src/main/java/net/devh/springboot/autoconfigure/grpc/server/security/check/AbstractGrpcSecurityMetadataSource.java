/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.springboot.autoconfigure.grpc.server.security.check;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;

import io.grpc.MethodDescriptor;

/**
 * Abstract implementation of {@link GrpcSecurityMetadataSource} which resolves the secured object type to a
 * {@link MethodDescriptor}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public abstract class AbstractGrpcSecurityMetadataSource implements GrpcSecurityMetadataSource {

    @Override
    public final Collection<ConfigAttribute> getAttributes(final Object object) throws IllegalArgumentException {
        if (object instanceof MethodDescriptor) {
            return getAttributes((MethodDescriptor<?, ?>) object);
        }
        throw new IllegalArgumentException("Object must be a non-null MethodDescriptor");
    }

    @Override
    public final boolean supports(final Class<?> clazz) {
        return MethodDescriptor.class.equals(clazz);
    }

}

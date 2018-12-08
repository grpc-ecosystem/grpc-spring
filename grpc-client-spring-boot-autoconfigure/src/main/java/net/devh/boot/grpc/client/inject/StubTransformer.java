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

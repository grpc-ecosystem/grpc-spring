/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

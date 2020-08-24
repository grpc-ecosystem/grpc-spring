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

package net.devh.boot.grpc.test.inject;

import io.grpc.CallOptions;
import io.grpc.Channel;

/**
 * Fake generated grpc class.
 */
public class CustomGrpc {

    public static CustomAccessibleStub custom(final Channel channel) {
        return new CustomAccessibleStub(channel);
    }

    public static FactoryMethodAccessibleStub newFactoryMethodAccessibleStubStub(final Channel channel) {
        return new FactoryMethodAccessibleStub(channel);
    }

    public static class CustomAccessibleStub extends CustomStub<CustomAccessibleStub> {

        private CustomAccessibleStub(final Channel channel) {
            super(channel);
        }

        private CustomAccessibleStub(final Channel channel, final CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected CustomAccessibleStub build(final Channel channel, final CallOptions callOptions) {
            return new CustomAccessibleStub(channel, callOptions);
        }

    }

    public static class FactoryMethodAccessibleStub extends OtherStub<FactoryMethodAccessibleStub> {

        private FactoryMethodAccessibleStub(final Channel channel) {
            super(channel);
        }

        private FactoryMethodAccessibleStub(final Channel channel, final CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected FactoryMethodAccessibleStub build(final Channel channel, final CallOptions callOptions) {
            return new FactoryMethodAccessibleStub(channel, callOptions);
        }

    }

    public static class ConstructorAccessibleStub extends OtherStub<ConstructorAccessibleStub> {

        public ConstructorAccessibleStub(final Channel channel) {
            super(channel);
        }

        public ConstructorAccessibleStub(final Channel channel, final CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected ConstructorAccessibleStub build(final Channel channel, final CallOptions callOptions) {
            return new ConstructorAccessibleStub(channel, callOptions);
        }

    }

}

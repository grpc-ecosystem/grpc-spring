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

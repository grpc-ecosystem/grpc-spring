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
import io.grpc.stub.AbstractStub;

/**
 * Simulates a custom stub type provided by a third party library, that requires a custom
 * {@link net.devh.boot.grpc.client.stubfactory.StubFactory StubFactory}.
 *
 * @param <S> The type of the stub implementation.
 */
public abstract class CustomStub<S extends CustomStub<S>> extends AbstractStub<S> {

    protected CustomStub(final Channel channel) {
        super(channel);
    }

    protected CustomStub(final Channel channel, final CallOptions callOptions) {
        super(channel, callOptions);
    }

}

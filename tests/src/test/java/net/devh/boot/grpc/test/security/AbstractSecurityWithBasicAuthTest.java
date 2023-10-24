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

package net.devh.boot.grpc.test.security;

import static io.grpc.Status.Code.UNAUTHENTICATED;

import org.junit.jupiter.api.TestFactory;
import org.springframework.test.annotation.DirtiesContext;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;
import net.devh.boot.grpc.test.util.DynamicTestCollection;

abstract class AbstractSecurityWithBasicAuthTest extends AbstractSecurityTest {

    @GrpcClient("unknownUser")
    protected TestServiceStub unknownUserStub;
    @GrpcClient("unknownUser")
    protected TestServiceBlockingStub unknownUserBlockingStub;
    @GrpcClient("unknownUser")
    protected TestServiceFutureStub unknownUserFutureStub;

    @GrpcClient("noAuth")
    protected TestServiceStub noAuthStub;
    @GrpcClient("noAuth")
    protected TestServiceBlockingStub noAuthBlockingStub;
    @GrpcClient("noAuth")
    protected TestServiceFutureStub noAuthFutureStub;

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection unprotectedCallTests() {
        return super.unprotectedCallTests()
                .add("unprotected-unknownUser",
                        () -> assertNormalCallFailure(this.unknownUserStub, this.unknownUserBlockingStub,
                                this.unknownUserFutureStub, UNAUTHENTICATED))
                .add("unprotected-noAuth",
                        () -> assertNormalCallSuccess(this.noAuthStub, this.noAuthBlockingStub, this.noAuthFutureStub));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection unaryCallTest() {
        return super.unaryCallTest()
                .add("unary-unknownUser",
                        () -> assertUnaryCallFailure(this.unknownUserStub, this.unknownUserBlockingStub,
                                this.unknownUserFutureStub, UNAUTHENTICATED))
                .add("unary-noAuth",
                        () -> assertUnaryCallFailure(this.noAuthStub, this.noAuthBlockingStub, this.noAuthFutureStub,
                                UNAUTHENTICATED));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection clientStreamingCallTests() {
        return super.clientStreamingCallTests()
                .add("clientStreaming-unknownUser",
                        () -> assertClientStreamingCallFailure(this.unknownUserStub, UNAUTHENTICATED))
                .add("clientStreaming-noAuth",
                        () -> assertClientStreamingCallFailure(this.noAuthStub, UNAUTHENTICATED));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection serverStreamingCallTests() {
        return super.serverStreamingCallTests()
                .add("serverStreaming-unknownUser",
                        () -> assertServerStreamingCallFailure(this.unknownUserStub, UNAUTHENTICATED))
                .add("serverStreaming-noAuth",
                        () -> assertServerStreamingCallFailure(this.noAuthStub, UNAUTHENTICATED));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection bidiStreamingCallTests() {
        return super.bidiStreamingCallTests()
                .add("bidiStreaming-unknownUser",
                        () -> assertServerStreamingCallFailure(this.unknownUserStub, UNAUTHENTICATED))
                .add("bidiStreaming-noAuth",
                        () -> assertServerStreamingCallFailure(this.noAuthStub, UNAUTHENTICATED));
    }

}

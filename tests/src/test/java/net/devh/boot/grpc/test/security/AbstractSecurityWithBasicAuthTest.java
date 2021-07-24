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

package net.devh.boot.grpc.test.security;

import static io.grpc.Status.Code.UNAUTHENTICATED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.annotation.DirtiesContext;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;
import net.devh.boot.grpc.test.util.DynamicTestCollection;

public abstract class AbstractSecurityWithBasicAuthTest extends AbstractSecurityTest {

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
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection unprotectedCallTests() {
        return super.unprotectedCallTests()
                .add("unprotected-unknownUser",
                        () -> assertNormalCallFailure(this.unknownUserStub, this.unknownUserBlockingStub,
                                this.unknownUserFutureStub, UNAUTHENTICATED))
                .add("unprotected-noAuth",
                        () -> assertNormalCallSuccess(this.noAuthStub, this.noAuthBlockingStub, this.noAuthFutureStub));
    }

    @Override
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection unaryCallTest() {
        return super.unaryCallTest()
                .add("unary-unknownUser",
                        () -> assertUnaryCallFailure(this.unknownUserStub, this.unknownUserBlockingStub,
                                this.unknownUserFutureStub, UNAUTHENTICATED))
                .add("unary-noAuth",
                        () -> assertUnaryCallFailure(this.noAuthStub, this.noAuthBlockingStub, this.noAuthFutureStub,
                                UNAUTHENTICATED));
    }

    @Override
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection clientStreamingCallTests() {
        return super.clientStreamingCallTests()
                .add("clientStreaming-unknownUser",
                        () -> assertClientStreamingCallFailure(this.unknownUserStub, UNAUTHENTICATED))
                .add("clientStreaming-noAuth",
                        () -> assertClientStreamingCallFailure(this.noAuthStub, UNAUTHENTICATED));
    }

    @Override
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection serverStreamingCallTests() {
        return super.serverStreamingCallTests()
                .add("serverStreaming-unknownUser",
                        () -> assertServerStreamingCallFailure(this.unknownUserStub, UNAUTHENTICATED))
                .add("serverStreaming-noAuth",
                        () -> assertServerStreamingCallFailure(this.noAuthStub, UNAUTHENTICATED));
    }

    @Override
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection bidiStreamingCallTests() {
        return super.bidiStreamingCallTests()
                .add("bidiStreaming-unknownUser",
                        () -> assertServerStreamingCallFailure(this.unknownUserStub, UNAUTHENTICATED))
                .add("bidiStreaming-noAuth",
                        () -> assertServerStreamingCallFailure(this.noAuthStub, UNAUTHENTICATED));
    }

}

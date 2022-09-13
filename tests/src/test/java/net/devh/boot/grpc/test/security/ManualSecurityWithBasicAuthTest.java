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

import static io.grpc.Status.Code.PERMISSION_DENIED;

import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.DualInProcessConfiguration;
import net.devh.boot.grpc.test.config.ManualSecurityConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.config.WithBasicAuthSecurityConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;
import net.devh.boot.grpc.test.util.DynamicTestCollection;

/**
 * A test checking that the server and client can start and connect to each other with minimal config.
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(
        classes = {ServiceConfiguration.class, DualInProcessConfiguration.class, BaseAutoConfiguration.class,
                ManualSecurityConfiguration.class, WithBasicAuthSecurityConfiguration.class})
@DirtiesContext
class ManualSecurityWithBasicAuthTest extends AbstractSecurityWithBasicAuthTest {

    // The secondary stubs use the secondary server

    @GrpcClient("test-secondary")
    protected TestServiceStub serviceStubSecondary;
    @GrpcClient("test-secondary")
    protected TestServiceBlockingStub blockingStubSecondary;
    @GrpcClient("test-secondary")
    protected TestServiceFutureStub futureStubSecondary;

    @GrpcClient("noPerm-secondary")
    protected TestServiceStub noPermStubSecondary;
    @GrpcClient("noPerm-secondary")
    protected TestServiceBlockingStub noPermBlockingStubSecondary;
    @GrpcClient("noPerm-secondary")
    protected TestServiceFutureStub noPermFutureStubSecondary;

    ManualSecurityWithBasicAuthTest() {
        log.info("--- ManualSecurityWithBasicAuthTest ---");
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection unprotectedCallTests() {
        return super.unprotectedCallTests()
                .add("unprotected-secondary",
                        () -> assertNormalCallSuccess(this.serviceStubSecondary, this.blockingStubSecondary,
                                this.futureStubSecondary))
                .add("unprotected-noPerm-secondary",
                        () -> assertNormalCallSuccess(this.noPermStubSecondary, this.noPermBlockingStubSecondary,
                                this.noPermFutureStubSecondary));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection unaryCallTest() {
        return super.unaryCallTest()
                .add("unary-secondary",
                        () -> assertUnaryCallSuccess(this.serviceStubSecondary, this.blockingStubSecondary,
                                this.futureStubSecondary))
                .add("unary-noPerm-secondary",
                        () -> assertUnaryCallFailure(this.noPermStubSecondary, this.noPermBlockingStubSecondary,
                                this.noPermFutureStubSecondary, PERMISSION_DENIED));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection clientStreamingCallTests() {
        return super.clientStreamingCallTests()
                .add("clientStreaming-secondary",
                        () -> assertClientStreamingCallFailure(this.serviceStubSecondary, PERMISSION_DENIED))
                .add("clientStreaming-noPerm-secondary",
                        () -> assertClientStreamingCallFailure(this.noPermStubSecondary, PERMISSION_DENIED));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection serverStreamingCallTests() {
        return super.serverStreamingCallTests()
                .add("serverStreaming-secondary",
                        () -> assertServerStreamingCallSuccess(this.serviceStubSecondary))
                .add("serverStreaming-noPerm-secondary",
                        () -> assertServerStreamingCallSuccess(this.noPermStubSecondary));
    }

    @Override
    @DirtiesContext
    @TestFactory
    DynamicTestCollection bidiStreamingCallTests() {
        return super.bidiStreamingCallTests()
                .add("bidiStreaming-secondary",
                        () -> assertServerStreamingCallSuccess(this.serviceStubSecondary))
                .add("bidiStreaming-noPerm-secondary",
                        () -> assertServerStreamingCallSuccess(this.noPermStubSecondary));
    }

}

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

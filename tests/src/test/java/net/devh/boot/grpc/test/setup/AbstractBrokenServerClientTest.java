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

package net.devh.boot.grpc.test.setup;

import static io.grpc.Status.Code.UNAVAILABLE;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertFutureThrowsStatus;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertThrowsStatus;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

@Slf4j
public abstract class AbstractBrokenServerClientTest {

    // Don't configure client
    @GrpcClient("test")
    protected Channel channel;
    @GrpcClient("test")
    protected TestServiceStub testServiceStub;
    @GrpcClient("test")
    protected TestServiceBlockingStub testServiceBlockingStub;
    @GrpcClient("test")
    protected TestServiceFutureStub testServiceFutureStub;

    /**
     * Test successful call with broken setup.
     */
    @Test
    @DirtiesContext
    public void testSuccessfulCallWithBrokenSetup() {
        log.info("--- Starting tests with successful call with broken setup ---");
        assertThrowsStatus(UNAVAILABLE,
                () -> TestServiceGrpc.newBlockingStub(this.channel).normal(Empty.getDefaultInstance()));

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.testServiceStub.normal(Empty.getDefaultInstance(), streamRecorder);
        assertFutureThrowsStatus(UNAVAILABLE, streamRecorder.firstValue(), 5, TimeUnit.SECONDS);
        assertThrowsStatus(UNAVAILABLE, () -> this.testServiceBlockingStub.normal(Empty.getDefaultInstance()));
        assertFutureThrowsStatus(UNAVAILABLE, this.testServiceFutureStub.normal(Empty.getDefaultInstance()),
                5, TimeUnit.SECONDS);
        log.info("--- Test completed ---");
    }

    /**
     * Test failing call with broken setup.
     */
    @Test
    @DirtiesContext
    public void testFailingCallWithBrokenSetup() {
        log.info("--- Starting tests with failing call with broken setup ---");
        assertThrowsStatus(UNAVAILABLE,
                () -> TestServiceGrpc.newBlockingStub(this.channel).unimplemented(Empty.getDefaultInstance()));

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.testServiceStub.unimplemented(Empty.getDefaultInstance(), streamRecorder);
        assertFutureThrowsStatus(UNAVAILABLE, streamRecorder.firstValue(), 5, TimeUnit.SECONDS);
        assertThrowsStatus(UNAVAILABLE, () -> this.testServiceBlockingStub.unimplemented(Empty.getDefaultInstance()));
        assertFutureThrowsStatus(UNAVAILABLE, this.testServiceFutureStub.unimplemented(Empty.getDefaultInstance()),
                5, TimeUnit.SECONDS);
        log.info("--- Test completed ---");
    }

}

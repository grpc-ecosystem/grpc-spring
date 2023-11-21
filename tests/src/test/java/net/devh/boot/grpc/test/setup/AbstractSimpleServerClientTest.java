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

import static io.grpc.Status.Code.UNIMPLEMENTED;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertFutureThrowsStatus;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertThrowsStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.internal.testing.StreamRecorder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

/**
 * A test checking that the server and client can start and connect to each other with minimal config.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public abstract class AbstractSimpleServerClientTest {

    @GrpcClient("test")
    protected Channel channel;
    @GrpcClient("test")
    protected TestServiceStub testServiceStub;
    @GrpcClient("test")
    protected TestServiceBlockingStub testServiceBlockingStub;
    @GrpcClient("test")
    protected TestServiceFutureStub testServiceFutureStub;

    @PostConstruct
    protected void init() {
        // Test injection
        assertNotNull(this.channel, "channel");
        assertNotNull(this.testServiceBlockingStub, "testServiceBlockingStub");
        assertNotNull(this.testServiceFutureStub, "testServiceFutureStub");
        assertNotNull(this.testServiceStub, "testServiceStub");
    }

    /**
     * Test successful call.
     *
     * @throws ExecutionException Should never happen.
     * @throws InterruptedException Should never happen.
     */
    @Test
    @DirtiesContext
    void testSuccessfulCall() throws InterruptedException, ExecutionException {
        log.info("--- Starting tests with successful call ---");
        assertEquals("1.2.3",
                TestServiceGrpc.newBlockingStub(this.channel).normal(Empty.getDefaultInstance()).getVersion());

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.testServiceStub.normal(Empty.getDefaultInstance(), streamRecorder);
        assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
        assertEquals("1.2.3", this.testServiceBlockingStub.normal(Empty.getDefaultInstance()).getVersion());
        assertEquals("1.2.3", this.testServiceFutureStub.normal(Empty.getDefaultInstance()).get().getVersion());
        log.info("--- Test completed ---");
    }

    /**
     * Test failing call.
     */
    @Test
    @DirtiesContext
    void testFailingCall() {
        log.info("--- Starting tests with failing call ---");
        assertThrowsStatus(UNIMPLEMENTED,
                () -> TestServiceGrpc.newBlockingStub(this.channel).unimplemented(Empty.getDefaultInstance()));

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.testServiceStub.unimplemented(Empty.getDefaultInstance(), streamRecorder);
        assertFutureThrowsStatus(UNIMPLEMENTED, streamRecorder.firstValue(), 5, TimeUnit.SECONDS);
        assertThrowsStatus(UNIMPLEMENTED, () -> this.testServiceBlockingStub.unimplemented(Empty.getDefaultInstance()));
        assertFutureThrowsStatus(UNIMPLEMENTED, this.testServiceFutureStub.unimplemented(Empty.getDefaultInstance()),
                5, TimeUnit.SECONDS);
        log.info("--- Test completed ---");
    }

}

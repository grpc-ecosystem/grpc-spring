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

package net.devh.boot.grpc.test.advice;

import static net.devh.boot.grpc.test.util.FutureAssertions.assertFutureThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

/**
 * A test checking that the server and client can start and connect to each other with proper exception handling.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
@Slf4j
abstract class AbstractSimpleServerClientTest {

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
     * Test template call to check for every exception.
     */
    <E extends RuntimeException> void testGrpcCallAndVerifyMappedException(Status expectedStatus, Metadata metadata) {

        verifyManualBlockingStubCall(expectedStatus, metadata);
        verifyBlockingStubCall(expectedStatus, metadata);
        verifyManualFutureStubCall(expectedStatus, metadata);
        verifyFutureStubCall(expectedStatus, metadata);
    }

    private <E extends RuntimeException> void verifyManualBlockingStubCall(
            Status expectedStatus, Metadata expectedMetadata) {

        StatusRuntimeException actualException =
                assertThrows(StatusRuntimeException.class,
                        () -> TestServiceGrpc.newBlockingStub(this.channel).normal(Empty.getDefaultInstance()));

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }

    private <E extends RuntimeException> void verifyBlockingStubCall(Status expectedStatus, Metadata expectedMetadata) {

        StatusRuntimeException actualException =
                assertThrows(StatusRuntimeException.class,
                        () -> this.testServiceBlockingStub.normal(Empty.getDefaultInstance()));

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }


    private <E extends RuntimeException> void verifyManualFutureStubCall(
            Status expectedStatus, Metadata expectedMetadata) {

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.testServiceStub.normal(Empty.getDefaultInstance(), streamRecorder);
        StatusRuntimeException actualException =
                assertFutureThrows(StatusRuntimeException.class, streamRecorder.firstValue(), 5, TimeUnit.SECONDS);

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }


    private <E extends RuntimeException> void verifyFutureStubCall(
            Status expectedStatus, Metadata expectedMetadata) {

        StatusRuntimeException actualException =
                assertFutureThrows(StatusRuntimeException.class,
                        this.testServiceFutureStub.normal(Empty.getDefaultInstance()),
                        5,
                        TimeUnit.SECONDS);

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }

    private void verifyStatusAndMetadata(
            StatusRuntimeException actualException, Status expectedStatus, Metadata expectedMetadata) {

        assertThat(actualException.getTrailers())
                .usingRecursiveComparison()
                .isEqualTo(expectedMetadata);
        assertThat(actualException.getStatus())
                .usingRecursiveComparison()
                .isEqualTo(expectedStatus);
    }

}

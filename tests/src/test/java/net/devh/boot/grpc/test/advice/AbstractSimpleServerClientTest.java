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

package net.devh.boot.grpc.test.advice;

import static net.devh.boot.grpc.test.util.GrpcAssertions.assertFutureThrowsStatus;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertThrowsStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;
import net.devh.boot.grpc.test.util.AwaitableStreamObserver;

/**
 * A test checking that the server and client can start and connect to each other with proper exception handling.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
abstract class AbstractSimpleServerClientTest {

    private static final Empty EMPTY = Empty.getDefaultInstance();
    private static final SomeType SOME_TYPE = SomeType.getDefaultInstance();

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
     *
     * @param expectedStatus The status to expect.
     * @param expectedMetadata The metadata to expect.
     */
    void testUnaryGrpcCallAndVerifyMappedException(final Status expectedStatus, final Metadata expectedMetadata) {

        verifyManualBlockingStubCall(expectedStatus, expectedMetadata);
        verifyBlockingStubCall(expectedStatus, expectedMetadata);
        verifyManualFutureStubCall(expectedStatus, expectedMetadata);
        verifyFutureStubCall(expectedStatus, expectedMetadata);

    }

    /**
     * Test template call to check for every exception.
     *
     * @param expectedStatus The status to expect.
     * @param expectedMetadata The metadata to expect.
     */
    void testStreamingGrpcCallAndVerifyMappedException(final Status expectedStatus, final Metadata expectedMetadata) {

        final AwaitableStreamObserver<SomeType> responseObserver = new AwaitableStreamObserver<>();
        final StreamObserver<SomeType> requestObserver = this.testServiceStub.echo(responseObserver);
        requestObserver.onNext(SOME_TYPE);
        requestObserver.onCompleted();

        final StatusRuntimeException error =
                (StatusRuntimeException) assertDoesNotThrow(() -> responseObserver.getError());
        verifyStatusAndMetadata(error, expectedStatus, expectedMetadata);

    }

    private void verifyManualBlockingStubCall(
            final Status expectedStatus, final Metadata expectedMetadata) {

        final TestServiceBlockingStub newBlockingStub = TestServiceGrpc.newBlockingStub(this.channel);
        final StatusRuntimeException actualException =
                assertThrowsStatus(() -> newBlockingStub.normal(EMPTY));

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }

    private void verifyBlockingStubCall(final Status expectedStatus,
            final Metadata expectedMetadata) {

        final StatusRuntimeException actualException =
                assertThrowsStatus(() -> this.testServiceBlockingStub.normal(EMPTY));

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }


    private void verifyManualFutureStubCall(
            final Status expectedStatus, final Metadata expectedMetadata) {

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.testServiceStub.normal(EMPTY, streamRecorder);
        final StatusRuntimeException actualException =
                assertFutureThrowsStatus(streamRecorder.firstValue(), 5, TimeUnit.SECONDS);

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }


    private void verifyFutureStubCall(
            final Status expectedStatus, final Metadata expectedMetadata) {

        final StatusRuntimeException actualException =
                assertFutureThrowsStatus(this.testServiceFutureStub.normal(EMPTY), 5, TimeUnit.SECONDS);

        verifyStatusAndMetadata(actualException, expectedStatus, expectedMetadata);
    }

    private void verifyStatusAndMetadata(
            final StatusRuntimeException actualException, final Status expectedStatus,
            final Metadata expectedMetadata) {

        assertThat(actualException.getTrailers())
                .usingRecursiveComparison()
                .isEqualTo(expectedMetadata);
        assertThat(actualException.getStatus())
                .usingRecursiveComparison()
                .isEqualTo(expectedStatus);
    }

}

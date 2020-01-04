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

package net.devh.boot.grpc.test.security;

import static io.grpc.Status.Code.PERMISSION_DENIED;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.devh.boot.grpc.test.util.FutureAssertions.assertFutureEquals;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertFutureFirstEquals;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertFutureThrowsStatus;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertThrowsStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.annotation.DirtiesContext;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;

import io.grpc.Status.Code;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;
import net.devh.boot.grpc.test.util.DynamicTestCollection;
import net.devh.boot.grpc.test.util.TriConsumer;

public abstract class AbstractSecurityTest {

    protected static final Empty EMPTY = Empty.getDefaultInstance();

    @GrpcClient("test")
    protected TestServiceStub serviceStub;
    @GrpcClient("test")
    protected TestServiceBlockingStub blockingStub;
    @GrpcClient("test")
    protected TestServiceFutureStub futureStub;

    @GrpcClient("noPerm")
    protected TestServiceStub noPermStub;
    @GrpcClient("noPerm")
    protected TestServiceBlockingStub noPermBlockingStub;
    @GrpcClient("noPerm")
    protected TestServiceFutureStub noPermFutureStub;

    /**
     * Tests for with unprotected methods.
     *
     * @return The tests.
     */
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection unprotectedCallTests() {
        return DynamicTestCollection.create()
                .add("unprotected-default",
                        () -> assertNormalCallSuccess(this.serviceStub, this.blockingStub, this.futureStub))
                .add("unprotected-noPerm",
                        () -> assertNormalCallSuccess(this.noPermStub, this.noPermBlockingStub, this.noPermFutureStub));
    }

    protected void assertNormalCallSuccess(final TestServiceStub serviceStub,
            final TestServiceBlockingStub blockingStub,
            final TestServiceFutureStub futureStub) {
        assertUnarySuccessfulMethod(serviceStub,
                TestServiceStub::normal, blockingStub,
                TestServiceBlockingStub::normal, futureStub,
                TestServiceFutureStub::normal);
    }

    protected void assertNormalCallFailure(final TestServiceStub serviceStub,
            final TestServiceBlockingStub blockingStub,
            final TestServiceFutureStub futureStub,
            final Code expectedCode) {
        assertUnaryFailingMethod(serviceStub,
                TestServiceStub::normal, blockingStub,
                TestServiceBlockingStub::normal, futureStub,
                TestServiceFutureStub::normal, expectedCode);
    }

    /**
     * Tests with unary call.
     *
     * @return The tests.
     */
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection unaryCallTest() {
        return DynamicTestCollection.create()
                .add("unary-default",
                        () -> assertUnaryCallSuccess(this.serviceStub, this.blockingStub, this.futureStub))
                .add("unary-noPerm",
                        () -> assertUnaryCallFailure(this.noPermStub, this.noPermBlockingStub, this.noPermFutureStub,
                                PERMISSION_DENIED));
    }

    protected void assertUnaryCallSuccess(final TestServiceStub serviceStub,
            final TestServiceBlockingStub blockingStub,
            final TestServiceFutureStub futureStub) {
        assertUnarySuccessfulMethod(serviceStub,
                TestServiceStub::secure, blockingStub,
                TestServiceBlockingStub::secure, futureStub,
                TestServiceFutureStub::secure);
    }

    protected void assertUnaryCallFailure(final TestServiceStub serviceStub,
            final TestServiceBlockingStub blockingStub,
            final TestServiceFutureStub futureStub,
            final Code expectedCode) {
        assertUnaryFailingMethod(serviceStub,
                TestServiceStub::secure, blockingStub,
                TestServiceBlockingStub::secure, futureStub,
                TestServiceFutureStub::secure, expectedCode);
    }

    /**
     * Tests with client streaming call.
     *
     * @return The tests.
     */
    @Test
    @DirtiesContext
    @TestFactory
    public DynamicTestCollection clientStreamingCallTests() {
        return DynamicTestCollection.create()
                .add("clientStreaming-default", () -> assertClientStreamingCallSuccess(this.serviceStub))
                .add("clientStreaming-noPerm",
                        () -> assertClientStreamingCallFailure(this.noPermStub, PERMISSION_DENIED));
    }

    protected void assertClientStreamingCallSuccess(final TestServiceStub serviceStub) {
        final StreamRecorder<Empty> responseRecorder = StreamRecorder.create();
        final StreamObserver<SomeType> requestObserver = serviceStub.secureDrain(responseRecorder);
        sendAndComplete(requestObserver, "1.2.3");
        assertFutureFirstEquals(EMPTY, responseRecorder, 15, TimeUnit.SECONDS);
    }

    protected void assertClientStreamingCallFailure(final TestServiceStub serviceStub, final Code expectedCode) {
        final StreamRecorder<Empty> responseRecorder = StreamRecorder.create();
        final StreamObserver<SomeType> requestObserver = serviceStub.secureDrain(responseRecorder);
        // Let the server throw an exception if he receives that (assert security):
        sendAndComplete(requestObserver, "explode");
        assertFutureThrowsStatus(expectedCode, responseRecorder, 15, SECONDS);
    }

    /**
     * Tests with server streaming call.
     *
     * @return The tests.
     */
    @Test
    @DirtiesContext
    public DynamicTestCollection serverStreamingCallTests() {
        return DynamicTestCollection.create()
                .add("serverStreaming-default",
                        () -> assertServerStreamingCallSuccess(this.serviceStub))
                .add("serverStreaming-noPerm",
                        () -> assertServerStreamingCallFailure(this.noPermStub, PERMISSION_DENIED));
    }

    protected void assertServerStreamingCallSuccess(final TestServiceStub testStub) {
        final StreamRecorder<SomeType> responseRecorder = StreamRecorder.create();
        testStub.secureSupply(EMPTY, responseRecorder);
        assertFutureFirstEquals("1.2.3", responseRecorder, SomeType::getVersion, 5, SECONDS);
    }

    protected void assertServerStreamingCallFailure(final TestServiceStub serviceStub, final Code expectedCode) {
        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        serviceStub.secureSupply(EMPTY, streamRecorder);
        assertFutureThrowsStatus(expectedCode, streamRecorder, 15, SECONDS);
    }

    /**
     * Tests with bidirectional streaming call.
     *
     * @return The tests.
     */
    @Test
    @DirtiesContext
    public DynamicTestCollection bidiStreamingCallTests() {
        return DynamicTestCollection.create()
                .add("bidiStreaming-default", () -> assertBidiCallSuccess(this.serviceStub))
                .add("bidiStreaming-noPerm", () -> assertBidiCallFailure(this.noPermStub, PERMISSION_DENIED));
    }

    protected void assertBidiCallSuccess(final TestServiceStub testStub) {
        final StreamRecorder<SomeType> responseRecorder = StreamRecorder.create();
        final StreamObserver<SomeType> requestObserver = testStub.secureBidi(responseRecorder);
        sendAndComplete(requestObserver, "1.2.3");
        assertFutureFirstEquals("1.2.3", responseRecorder, SomeType::getVersion, 5, SECONDS);
    }

    protected void assertBidiCallFailure(final TestServiceStub serviceStub, final Code expectedCode) {
        final StreamRecorder<SomeType> responseRecorder = StreamRecorder.create();
        final StreamObserver<SomeType> requestObserver = serviceStub.secureBidi(responseRecorder);
        sendAndComplete(requestObserver, "explode");
        assertFutureThrowsStatus(expectedCode, responseRecorder, 15, SECONDS);
    }

    // -------------------------------------

    protected void assertUnarySuccessfulMethod(final TestServiceStub serviceStub,
            final TriConsumer<TestServiceStub, Empty, StreamRecorder<SomeType>> serviceCall,
            final TestServiceBlockingStub blockingStub,
            final BiFunction<TestServiceBlockingStub, Empty, SomeType> blockingcall,
            final TestServiceFutureStub futureStub,
            final BiFunction<TestServiceFutureStub, Empty, ListenableFuture<SomeType>> futureCall) {

        final StreamRecorder<SomeType> responseRecorder = StreamRecorder.create();
        serviceCall.accept(serviceStub, EMPTY, responseRecorder);
        assertFutureFirstEquals("1.2.3", responseRecorder, SomeType::getVersion, 5, SECONDS);

        assertEquals("1.2.3", blockingcall.apply(blockingStub, EMPTY).getVersion());
        assertFutureEquals("1.2.3", futureCall.apply(futureStub, EMPTY), SomeType::getVersion, 5, SECONDS);
    }

    protected void assertUnaryFailingMethod(final TestServiceStub serviceStub,
            final TriConsumer<TestServiceStub, Empty, StreamRecorder<SomeType>> serviceCall,
            final TestServiceBlockingStub blockingStub,
            final BiFunction<TestServiceBlockingStub, Empty, SomeType> blockingcall,
            final TestServiceFutureStub futureStub,
            final BiFunction<TestServiceFutureStub, Empty, ListenableFuture<SomeType>> futureCall,
            final Code expectedCode) {

        final StreamRecorder<SomeType> responseRecorder = StreamRecorder.create();
        serviceCall.accept(serviceStub, EMPTY, responseRecorder);
        assertFutureThrowsStatus(expectedCode, responseRecorder, 5, SECONDS);

        assertThrowsStatus(expectedCode, () -> blockingcall.apply(blockingStub, EMPTY));
        assertFutureThrowsStatus(expectedCode, futureCall.apply(futureStub, EMPTY), 5, SECONDS);
    }

    protected void sendAndComplete(final StreamObserver<SomeType> requestObserver, final String message) {
        requestObserver.onNext(SomeType.newBuilder().setVersion(message).build());
        requestObserver.onNext(SomeType.newBuilder().setVersion(message).build());
        requestObserver.onNext(SomeType.newBuilder().setVersion(message).build());
        requestObserver.onCompleted();
    }

}

/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.test.grpc.security;

import static io.grpc.Status.Code.PERMISSION_DENIED;
import static net.devh.test.grpc.util.GrpcAssertions.assertFutureThrowsStatus;
import static net.devh.test.grpc.util.GrpcAssertions.assertThrowsStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import net.devh.test.grpc.proto.SomeType;
import net.devh.test.grpc.proto.TestServiceGrpc;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceStub;

@Slf4j
public abstract class AbstractSecurityTest {

    @GrpcClient("test")
    protected Channel channel;
    @GrpcClient("test")
    protected TestServiceStub testServiceStub;
    @GrpcClient("test")
    protected TestServiceBlockingStub testServiceBlockingStub;
    @GrpcClient("test")
    protected TestServiceFutureStub testServiceFutureStub;

    @GrpcClient("broken")
    protected Channel brokenChannel;
    @GrpcClient("broken")
    protected TestServiceStub brokenTestServiceStub;
    @GrpcClient("broken")
    protected TestServiceBlockingStub brokenTestServiceBlockingStub;
    @GrpcClient("broken")
    protected TestServiceFutureStub brokenTestServiceFutureStub;

    /**
     * Test successful call.
     *
     * @throws ExecutionException Should never happen.
     * @throws InterruptedException Should never happen.
     */
    @Test
    @DirtiesContext
    public void testSuccessfulCall() throws InterruptedException, ExecutionException {
        log.info("--- Starting tests with successful call ---");
        {
            assertEquals("1.2.3",
                    TestServiceGrpc.newBlockingStub(this.channel).normal(Empty.getDefaultInstance()).getVersion());

            final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
            this.testServiceStub.normal(Empty.getDefaultInstance(), streamRecorder);
            assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
            assertEquals("1.2.3", this.testServiceBlockingStub.normal(Empty.getDefaultInstance()).getVersion());
            assertEquals("1.2.3", this.testServiceFutureStub.normal(Empty.getDefaultInstance()).get().getVersion());
        }

        {
            assertEquals("1.2.3",
                    TestServiceGrpc.newBlockingStub(this.brokenChannel).normal(Empty.getDefaultInstance())
                            .getVersion());

            final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
            this.brokenTestServiceStub.normal(Empty.getDefaultInstance(), streamRecorder);
            assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
            assertEquals("1.2.3", this.brokenTestServiceBlockingStub.normal(Empty.getDefaultInstance()).getVersion());
            assertEquals("1.2.3",
                    this.brokenTestServiceFutureStub.normal(Empty.getDefaultInstance()).get().getVersion());
        }

        log.info("--- Test completed ---");
    }

    /**
     * Test secured call.
     *
     * @throws ExecutionException Should never happen.
     * @throws InterruptedException Should never happen.
     */
    @Test
    @DirtiesContext
    public void testSecuredCall() throws InterruptedException, ExecutionException {
        log.info("--- Starting tests with secured call ---");
        {
            assertEquals("1.2.3",
                    TestServiceGrpc.newBlockingStub(this.channel).secure(Empty.getDefaultInstance()).getVersion());

            final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
            this.testServiceStub.secure(Empty.getDefaultInstance(), streamRecorder);
            assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
            assertEquals("1.2.3", this.testServiceBlockingStub.secure(Empty.getDefaultInstance()).getVersion());
            assertEquals("1.2.3", this.testServiceFutureStub.secure(Empty.getDefaultInstance()).get().getVersion());
        }

        {
            assertThrowsStatus(PERMISSION_DENIED,
                    () -> TestServiceGrpc.newBlockingStub(this.brokenChannel).secure(Empty.getDefaultInstance()));

            final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
            this.brokenTestServiceStub.secure(Empty.getDefaultInstance(), streamRecorder);
            assertFutureThrowsStatus(PERMISSION_DENIED, streamRecorder.firstValue(), 5, TimeUnit.SECONDS);
            assertThrowsStatus(PERMISSION_DENIED,
                    () -> this.brokenTestServiceBlockingStub.secure(Empty.getDefaultInstance()));
            assertFutureThrowsStatus(PERMISSION_DENIED,
                    this.brokenTestServiceFutureStub.secure(Empty.getDefaultInstance()), 5, TimeUnit.SECONDS);
        }
        log.info("--- Test completed ---");
    }

}

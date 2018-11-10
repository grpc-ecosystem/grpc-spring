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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutionException;

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
public abstract class AbstractSecurityWithBasicAuthTest extends AbstractSecurityTest {

    @GrpcClient(value = "bean", interceptorNames = "basicAuthInterceptor")
    protected Channel beanChannel;
    @GrpcClient(value = "bean", interceptorNames = "basicAuthInterceptor")
    protected TestServiceStub beanTestServiceStub;
    @GrpcClient(value = "bean", interceptorNames = "basicAuthInterceptor")
    protected TestServiceBlockingStub beanTestServiceBlockingStub;
    @GrpcClient(value = "bean", interceptorNames = "basicAuthInterceptor")
    protected TestServiceFutureStub beanTestServiceFutureStub;

    /**
     * Test successful call.
     *
     * @throws ExecutionException Should never happen.
     * @throws InterruptedException Should never happen.
     */
    @Test
    @DirtiesContext
    public void testSuccessfulCallBean() throws InterruptedException, ExecutionException {
        log.info("--- Starting tests with successful call bean ---");
        assertEquals("1.2.3",
                TestServiceGrpc.newBlockingStub(this.beanChannel).normal(Empty.getDefaultInstance()).getVersion());
        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.beanTestServiceStub.normal(Empty.getDefaultInstance(), streamRecorder);
        assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
        assertEquals("1.2.3", this.beanTestServiceBlockingStub.normal(Empty.getDefaultInstance()).getVersion());
        assertEquals("1.2.3", this.beanTestServiceFutureStub.normal(Empty.getDefaultInstance()).get().getVersion());
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
    public void testSecuredCallBean() throws InterruptedException, ExecutionException {
        log.info("--- Starting tests with secured call bean ---");
        assertEquals("1.2.3",
                TestServiceGrpc.newBlockingStub(this.beanChannel).secure(Empty.getDefaultInstance()).getVersion());

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.beanTestServiceStub.secure(Empty.getDefaultInstance(), streamRecorder);
        assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
        assertEquals("1.2.3", this.beanTestServiceBlockingStub.secure(Empty.getDefaultInstance()).getVersion());
        assertEquals("1.2.3", this.beanTestServiceFutureStub.secure(Empty.getDefaultInstance()).get().getVersion());
        log.info("--- Test completed ---");
    }

}

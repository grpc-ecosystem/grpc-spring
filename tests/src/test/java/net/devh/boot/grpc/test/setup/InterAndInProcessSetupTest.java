/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.test.setup;

import static io.grpc.Status.Code.UNIMPLEMENTED;
import static net.devh.boot.grpc.test.proto.TestServiceGrpc.newBlockingStub;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertFutureThrowsStatus;
import static net.devh.boot.grpc.test.util.GrpcAssertions.assertThrowsStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

/**
 * Tests whether the parallel setup of inter- and in-process-server/client works.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest(properties = {
        "grpc.server.inProcessName=test",
        "grpc.server.port=9191",
        "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
        "grpc.client.inProcess.address=in-process:test",
        "grpc.client.interProcess.address=static://localhost:9191"})
@EnableConfigurationProperties(GrpcServerProperties.class)
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
public class InterAndInProcessSetupTest {

    private static final Empty EMPTY = Empty.getDefaultInstance();

    @GrpcClient("interProcess")
    protected Channel interProcessChannel;
    @GrpcClient("interProcess")
    protected TestServiceStub interProcessServiceStub;
    @GrpcClient("interProcess")
    protected TestServiceBlockingStub interProcessServiceBlockingStub;
    @GrpcClient("interProcess")
    protected TestServiceFutureStub interProcessServiceFutureStub;

    @GrpcClient("inProcess")
    protected Channel inProcessChannel;
    @GrpcClient("inProcess")
    protected TestServiceStub inProcessServiceStub;
    @GrpcClient("inProcess")
    protected TestServiceBlockingStub inProcessServiceBlockingStub;
    @GrpcClient("inProcess")
    protected TestServiceFutureStub inProcessServiceFutureStub;

    public InterAndInProcessSetupTest() {
        log.info("--- InterAndInProcessSetupTest ---");
    }

    @PostConstruct
    public void init() {
        // Test injection
        assertNotNull(this.interProcessChannel, "interProcessChannel");
        assertNotNull(this.interProcessServiceBlockingStub, "interProcessServiceBlockingStub");
        assertNotNull(this.interProcessServiceFutureStub, "interProcessServiceFutureStub");
        assertNotNull(this.interProcessServiceStub, "interProcessServiceStub");

        assertNotNull(this.inProcessChannel, "inProcessChannel");
        assertNotNull(this.inProcessServiceBlockingStub, "inProcessServiceBlockingStub");
        assertNotNull(this.inProcessServiceFutureStub, "inProcessServiceFutureStub");
        assertNotNull(this.inProcessServiceStub, "inProcessServiceStub");
    }

    /**
     * Test successful call for inter-process server.
     *
     * @throws ExecutionException Should never happen.
     * @throws InterruptedException Should never happen.
     */
    @Test
    @DirtiesContext
    public void testSuccessfulInterProcessCall() throws InterruptedException, ExecutionException {
        log.info("--- Starting tests with successful inter-process call ---");
        assertEquals("1.2.3", newBlockingStub(this.interProcessChannel).normal(EMPTY).getVersion());

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.interProcessServiceStub.normal(EMPTY, streamRecorder);
        assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
        assertEquals("1.2.3", this.interProcessServiceBlockingStub.normal(EMPTY).getVersion());
        assertEquals("1.2.3", this.interProcessServiceFutureStub.normal(EMPTY).get().getVersion());
        log.info("--- Test completed ---");
    }

    /**
     * Test successful call for in-process server.
     *
     * @throws ExecutionException Should never happen.
     * @throws InterruptedException Should never happen.
     */
    @Test
    @DirtiesContext
    public void testSuccessfulInProcessCall() throws InterruptedException, ExecutionException {
        log.info("--- Starting tests with successful in-process call ---");
        assertEquals("1.2.3", newBlockingStub(this.inProcessChannel).normal(EMPTY).getVersion());

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.inProcessServiceStub.normal(EMPTY, streamRecorder);
        assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion());
        assertEquals("1.2.3", this.inProcessServiceBlockingStub.normal(EMPTY).getVersion());
        assertEquals("1.2.3", this.inProcessServiceFutureStub.normal(EMPTY).get().getVersion());
        log.info("--- Test completed ---");
    }

    /**
     * Test failing call for inter-process server.
     */
    @Test
    @DirtiesContext
    public void testFailingInterProcessCall() {
        log.info("--- Starting tests with failing inter-process call ---");
        assertThrowsStatus(UNIMPLEMENTED, () -> newBlockingStub(this.interProcessChannel).unimplemented(EMPTY));

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.interProcessServiceStub.unimplemented(EMPTY, streamRecorder);
        assertFutureThrowsStatus(UNIMPLEMENTED, streamRecorder.firstValue(), 5, TimeUnit.SECONDS);
        assertThrowsStatus(UNIMPLEMENTED, () -> this.interProcessServiceBlockingStub.unimplemented(EMPTY));
        assertFutureThrowsStatus(UNIMPLEMENTED, this.interProcessServiceFutureStub.unimplemented(EMPTY),
                5, TimeUnit.SECONDS);
        log.info("--- Test completed ---");
    }

    /**
     * Test failing call for in-process server.
     */
    @Test
    @DirtiesContext
    public void testFailingInProcessCall() {
        log.info("--- Starting tests with failing in-process call ---");
        assertThrowsStatus(UNIMPLEMENTED, () -> newBlockingStub(this.inProcessChannel).unimplemented(EMPTY));

        final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
        this.inProcessServiceStub.unimplemented(EMPTY, streamRecorder);
        assertFutureThrowsStatus(UNIMPLEMENTED, streamRecorder.firstValue(), 5, TimeUnit.SECONDS);
        assertThrowsStatus(UNIMPLEMENTED, () -> this.inProcessServiceBlockingStub.unimplemented(EMPTY));
        assertFutureThrowsStatus(UNIMPLEMENTED, this.inProcessServiceFutureStub.unimplemented(EMPTY),
                5, TimeUnit.SECONDS);
        log.info("--- Test completed ---");
    }

}

package net.devh.test.grpc;

import static io.grpc.Status.Code.UNAVAILABLE;
import static net.devh.test.grpc.util.GrpcAssertions.assertFutureThrowsStatus;
import static net.devh.test.grpc.util.GrpcAssertions.assertThrowsStatus;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import net.devh.test.grpc.proto.Counter;
import net.devh.test.grpc.proto.TestServiceGrpc;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceStub;
import net.devh.test.grpc.proto.Version;

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
                () -> TestServiceGrpc.newBlockingStub(this.channel).getVersion(Empty.getDefaultInstance()));

        final StreamRecorder<Version> streamRecorder = StreamRecorder.create();
        this.testServiceStub.getVersion(Empty.getDefaultInstance(), streamRecorder);
        assertFutureThrowsStatus(UNAVAILABLE, streamRecorder.firstValue());
        assertThrowsStatus(UNAVAILABLE, () -> this.testServiceBlockingStub.getVersion(Empty.getDefaultInstance()));
        assertFutureThrowsStatus(UNAVAILABLE, this.testServiceFutureStub.getVersion(Empty.getDefaultInstance()));
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
                () -> TestServiceGrpc.newBlockingStub(this.channel).increment(Empty.getDefaultInstance()));

        final StreamRecorder<Counter> streamRecorder = StreamRecorder.create();
        this.testServiceStub.increment(Empty.getDefaultInstance(), streamRecorder);
        assertFutureThrowsStatus(UNAVAILABLE, streamRecorder.firstValue());
        assertThrowsStatus(UNAVAILABLE, () -> this.testServiceBlockingStub.increment(Empty.getDefaultInstance()));
        assertFutureThrowsStatus(UNAVAILABLE, this.testServiceFutureStub.increment(Empty.getDefaultInstance()));
        log.info("--- Test completed ---");
    }

}

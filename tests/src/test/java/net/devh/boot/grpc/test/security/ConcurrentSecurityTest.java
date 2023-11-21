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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.Status.Code;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.ManualSecurityConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.config.WithBasicAuthSecurityConfiguration;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;
import net.devh.boot.grpc.test.util.GrpcAssertions;

/**
 * Test that ensures that the security also works in concurrent environments. This seems to be a common problem in many
 * security examples. This test can also be used to simulate heavy load on the server, you just have to increase the
 * {@code parallelCount} drastically.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(
        classes = {ServiceConfiguration.class, InProcessConfiguration.class, BaseAutoConfiguration.class,
                ManualSecurityConfiguration.class, WithBasicAuthSecurityConfiguration.class})
@DirtiesContext
public class ConcurrentSecurityTest {

    @Autowired
    private GrpcServerLifecycle serverLifecycle;

    @GrpcClient("test")
    protected TestServiceStub testServiceStub;

    @GrpcClient("noPerm")
    protected TestServiceStub brokenTestServiceStub;

    /**
     * Test secured call.
     *
     * @throws Throwable Should never happen.
     */
    @Test
    @DirtiesContext
    public void testSecuredCall() throws Throwable {
        assertTrue("Server should be running", this.serverLifecycle.isRunning());
        final int parallelCount = 10; // Limited for automated tests, increase for in depth tests
        log.info("--- Starting tests with secured call ---");
        final List<Executable> runnables = new ArrayList<>();
        for (int i = 0; i < parallelCount; i++) {
            final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
            this.testServiceStub.secure(Empty.getDefaultInstance(), streamRecorder);
            runnables.add(() -> assertEquals("1.2.3", streamRecorder.firstValue().get().getVersion()));
        }
        for (int i = 0; i < parallelCount; i++) {
            final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
            this.brokenTestServiceStub.secure(Empty.getDefaultInstance(), streamRecorder);
            runnables.add(() -> GrpcAssertions.assertFutureThrowsStatus(Code.PERMISSION_DENIED,
                    streamRecorder.firstValue(), 15, TimeUnit.SECONDS));
        }
        Collections.shuffle(runnables);
        for (final Executable executable : runnables) {
            executable.execute();
        }
        log.info("--- Test completed ---");
    }

}

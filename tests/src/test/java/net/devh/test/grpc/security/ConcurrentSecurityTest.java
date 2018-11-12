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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.Empty;

import io.grpc.Status.Code;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import net.devh.test.grpc.config.ManualSecurityConfiguration;
import net.devh.test.grpc.config.ServiceConfiguration;
import net.devh.test.grpc.config.WithBasicAuthSecurityConfiguration;
import net.devh.test.grpc.proto.SomeType;
import net.devh.test.grpc.proto.TestServiceGrpc.TestServiceStub;
import net.devh.test.grpc.util.GrpcAssertions;

@Slf4j
@SpringBootTest(properties = {
        "grpc.client.test.negotiationType=PLAINTEXT",
        "grpc.client.broken.negotiationType=PLAINTEXT"})
@SpringJUnitConfig(classes = {ServiceConfiguration.class, ManualSecurityConfiguration.class,
        WithBasicAuthSecurityConfiguration.class})
@DirtiesContext
public class ConcurrentSecurityTest {

    @GrpcClient("test")
    protected TestServiceStub testServiceStub;

    @GrpcClient("broken")
    protected TestServiceStub brokenTestServiceStub;

    /**
     * Test secured call.
     *
     * @throws Throwable
     */
    @Test
    @DirtiesContext
    public void testSecuredCall() throws Throwable {
        final int parallelCount = 50; // Limited for automated tests, increase for in depth tests
        log.info("--- Starting tests with secured call ---");
        List<Executable> runnables = new ArrayList<>();
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
        for (Executable executable : runnables) {
            executable.execute();
        }
        log.info("--- Test completed ---");
    }

}

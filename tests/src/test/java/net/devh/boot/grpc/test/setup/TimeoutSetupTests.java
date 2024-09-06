/*
 * Copyright (c) 2016-2024 The gRPC-Spring Authors
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.SomeType;

/**
 * These tests check the property {@link GrpcChannelProperties#getTimeout()} ()}.
 */
public class TimeoutSetupTests {

    @Slf4j
    @SpringBootTest(properties = {
            "grpc.client.GLOBAL.address=localhost:9090",
            "grpc.client.GLOBAL.timeout=1s",
            "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
    })
    @SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
    static class TimeoutSetupTest extends AbstractSimpleServerClientTest {

        @Test
        @SneakyThrows
        @DirtiesContext
        void testServiceStubTimeoutEnabledAndSuccessful() {
            log.info("--- Starting test with unsuccessful and than successful call ---");
            final StreamRecorder<SomeType> streamRecorder1 = StreamRecorder.create();
            this.testServiceStub.echo(streamRecorder1);
            assertThrows(ExecutionException.class, () -> streamRecorder1.firstValue().get());

            final StreamRecorder<SomeType> streamRecorder2 = StreamRecorder.create();
            StreamObserver<SomeType> echo2 = testServiceStub.echo(streamRecorder2);
            echo2.onNext(SomeType.getDefaultInstance());
            assertNull(streamRecorder2.getError());
            assertNotNull(streamRecorder2.firstValue().get().getVersion());
            log.info("--- Test completed --- ");
        }

        @Test
        @SneakyThrows
        @DirtiesContext
        void testServiceStubManuallyConfiguredDeadlineTakesPrecedenceOfTheConfigOne() {
            log.info("--- Starting test that manually configured deadline takes precedence of the config timeout ---");
            final StreamRecorder<SomeType> streamRecorder = StreamRecorder.create();
            StreamObserver<SomeType> echo =
                    this.testServiceStub.withDeadlineAfter(5L, TimeUnit.SECONDS).echo(streamRecorder);
            TimeUnit.SECONDS.sleep(2);
            echo.onNext(SomeType.getDefaultInstance());
            assertNull(streamRecorder.getError());
            assertNotNull(streamRecorder.firstValue().get().getVersion());
            log.info("--- Test completed --- ");
        }
    }

    @Slf4j
    @SpringBootTest(properties = {
            "grpc.client.GLOBAL.address=localhost:9090",
            "grpc.client.GLOBAL.timeout=0s",
            "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
    })
    @SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
    static class ZeroTimeoutSetupTest extends AbstractSimpleServerClientTest {
    }

}

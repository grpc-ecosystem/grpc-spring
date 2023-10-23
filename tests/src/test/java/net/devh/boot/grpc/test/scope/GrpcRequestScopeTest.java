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

package net.devh.boot.grpc.test.scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.ScopedServiceConfiguration;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

@Slf4j
@SpringBootTest
@SpringJUnitConfig(
        classes = {InProcessConfiguration.class, ScopedServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
public class GrpcRequestScopeTest {

    @GrpcClient("test")
    protected TestServiceStub testServiceStub;

    @Test
    @DirtiesContext
    public void testScope() throws InterruptedException {
        // Prepare
        ScopedStreamObserverChecker scope1 = new ScopedStreamObserverChecker();
        StreamObserver<SomeType> request1 = this.testServiceStub.secureBidi(scope1);
        ScopedStreamObserverChecker scope2 = new ScopedStreamObserverChecker();
        StreamObserver<SomeType> request2 = this.testServiceStub.secureBidi(scope2);

        // Run
        request1.onNext(SomeType.getDefaultInstance());
        request1.onNext(SomeType.getDefaultInstance());
        Thread.sleep(100);

        request2.onNext(SomeType.getDefaultInstance());
        request2.onNext(SomeType.getDefaultInstance());
        Thread.sleep(100);

        request1.onNext(SomeType.getDefaultInstance());
        request2.onNext(SomeType.getDefaultInstance());
        Thread.sleep(100);

        request2.onNext(SomeType.getDefaultInstance());
        request1.onNext(SomeType.getDefaultInstance());
        Thread.sleep(100);

        request1.onCompleted();
        request2.onCompleted();
        Thread.sleep(100);

        // Assert
        assertTrue(scope1.isCompleted());
        assertTrue(scope2.isCompleted());
        assertNull(scope1.getError());
        assertNull(scope2.getError());
        assertNotNull(scope1.getText());
        assertNotNull(scope2.getText());
        assertNotEquals(scope1.getText(), scope2.getText());
        log.debug("A: {} - B: {}", scope1.getText(), scope2.getText());
    }

    /**
     * Helper class used to check that the scoped responses are different per request, but the same for different
     * messages in the same request.
     */
    private static class ScopedStreamObserverChecker implements StreamObserver<SomeType> {

        private String text;
        private boolean completed = false;
        private Throwable error;

        @Override
        public void onNext(SomeType value) {
            if (this.text == null) {
                this.text = value.getVersion();
            }
            try {
                assertEquals(this.text, value.getVersion());
            } catch (AssertionFailedError e) {
                if (this.error == null) {
                    this.error = e;
                } else {
                    this.error.addSuppressed(e);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (this.error == null) {
                this.error = t;
            } else {
                this.error.addSuppressed(t);
            }
            this.completed = true;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }

        public String getText() {
            return this.text;
        }

        public boolean isCompleted() {
            return this.completed;
        }

        public Throwable getError() {
            return this.error;
        }

    }

}

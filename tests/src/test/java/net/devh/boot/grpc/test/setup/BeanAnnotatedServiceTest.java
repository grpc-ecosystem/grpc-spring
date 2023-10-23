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

package net.devh.boot.grpc.test.setup;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.BeanAnnotatedServiceConfig;
import net.devh.boot.grpc.test.config.InProcessConfiguration;

/**
 * A test checking that the server picks up a {@link GrpcService} annotated bean from a {@link Configuration}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = {
        InProcessConfiguration.class,
        BeanAnnotatedServiceConfig.class,
        BaseAutoConfiguration.class})
@DirtiesContext
class BeanAnnotatedServiceTest extends AbstractSimpleServerClientTest {

    public BeanAnnotatedServiceTest() {
        log.info("--- BeanAnnotatedServiceTest ---");
    }

    @Autowired
    AtomicBoolean invoked;

    @Override
    @Test
    void testSuccessfulCall() throws InterruptedException, ExecutionException {
        assertFalse(this.invoked.get());
        super.testSuccessfulCall();
        assertTrue(this.invoked.get());
    }

}

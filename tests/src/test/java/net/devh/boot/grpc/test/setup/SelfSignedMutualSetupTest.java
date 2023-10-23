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

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;

/**
 * A test checking that the server and client can start and connect to each other with minimal config.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest(properties = {
        "grpc.server.security.enabled=true",
        "grpc.server.security.certificateChain=file:src/test/resources/certificates/server.crt",
        "grpc.server.security.privateKey=file:src/test/resources/certificates/server.key",
        "grpc.server.security.trustCertCollection=file:src/test/resources/certificates/trusted-clients-collection",
        "grpc.server.security.clientAuth=REQUIRE",

        "grpc.client.test.address=localhost:9090",
        "grpc.client.test.security.authorityOverride=localhost",
        "grpc.client.test.security.trustCertCollection=file:src/test/resources/certificates/trusted-servers-collection",
        "grpc.client.test.security.clientAuthEnabled=true",
        "grpc.client.test.security.certificateChain=file:src/test/resources/certificates/client1.crt",
        "grpc.client.test.security.privateKey=file:src/test/resources/certificates/client1.key"})
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
public class SelfSignedMutualSetupTest extends AbstractSimpleServerClientTest {

    public SelfSignedMutualSetupTest() {
        log.info("--- SelfSignedMutualSetupTest ---");
    }

}

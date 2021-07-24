/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.test.security;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ManualSecurityConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.config.WithCertificateSecurityConfiguration;

@Slf4j
@SpringBootTest(properties = {
        "grpc.server.security.enabled=true",
        "grpc.server.security.certificateChain=file:src/test/resources/certificates/server.crt",
        "grpc.server.security.privateKey=file:src/test/resources/certificates/server.key",
        "grpc.server.security.trustCertCollection=file:src/test/resources/certificates/trusted-clients-collection",
        "grpc.server.security.clientAuth=REQUIRE",

        "grpc.client.GLOBAL.address=localhost:9090",
        "grpc.client.GLOBAL.security.authorityOverride=localhost",
        "grpc.client.GLOBAL.security.trustCertCollection=file:src/test/resources/certificates/trusted-servers-collection",
        "grpc.client.GLOBAL.security.clientAuthEnabled=true",

        "grpc.client.test.security.certificateChain=file:src/test/resources/certificates/client1.crt",
        "grpc.client.test.security.privateKey=file:src/test/resources/certificates/client1.key",

        "grpc.client.noPerm.security.certificateChain=file:src/test/resources/certificates/client2.crt",
        "grpc.client.noPerm.security.privateKey=file:src/test/resources/certificates/client2.key"
})
@SpringJUnitConfig(
        classes = {ServiceConfiguration.class, BaseAutoConfiguration.class, ManualSecurityConfiguration.class,
                WithCertificateSecurityConfiguration.class})
@DirtiesContext
public class ManualSecurityWithCertificateTest extends AbstractSecurityTest {

    public ManualSecurityWithCertificateTest() {
        log.info("--- ManualSecurityWithCertificateTest ---");
    }

}

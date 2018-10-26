package net.devh.test.grpc;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClientAutoConfiguration;
import net.devh.springboot.autoconfigure.grpc.server.GrpcServerAutoConfiguration;
import net.devh.test.grpc.config.ServiceTestConfiguration;

/**
 * A test checking that the server and client can start and connect to each other with minimal
 * config.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@SpringBootTest(properties = {
        "grpc.server.security.enabled=true",
        "grpc.server.security.certificateChainPath=src/test/resources/certificates/server.crt",
        "grpc.server.security.privateKeyPath=src/test/resources/certificates/server.key",
        "grpc.server.security.trustCertCollectionPath=src/test/resources/certificates/client2.crt", // Wrong certs
        "grpc.server.security.clientAuth=REQUIRE",
        "grpc.client.test.security.authorityOverride=localhost",
        "grpc.client.test.security.trustCertCollectionPath=src/test/resources/certificates/trusted-servers-collection",
        "grpc.client.test.security.clientAuthEnabled=true",
        "grpc.client.test.security.certificateChainPath=src/test/resources/certificates/client1.crt",
        "grpc.client.test.security.privateKeyPath=src/test/resources/certificates/client1.key"})
@SpringJUnitConfig(classes = ServiceTestConfiguration.class)
@ImportAutoConfiguration({GrpcServerAutoConfiguration.class, GrpcClientAutoConfiguration.class})
@DirtiesContext
public class BrokenServerSelfSignedMutualSetupTest extends AbstractBrokenServerClientTest {

    public BrokenServerSelfSignedMutualSetupTest() {
        log.info("--- BrokenServerSelfSignedMutualSetupTest ---");
    }

}

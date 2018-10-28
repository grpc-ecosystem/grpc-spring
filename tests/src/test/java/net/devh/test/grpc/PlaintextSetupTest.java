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
@SpringBootTest(properties = "grpc.client.test.negotiationType=PLAINTEXT")
@SpringJUnitConfig(classes = ServiceTestConfiguration.class)
@ImportAutoConfiguration({GrpcServerAutoConfiguration.class, GrpcClientAutoConfiguration.class})
@DirtiesContext
public class PlaintextSetupTest extends AbstractSimpleServerClientTest {

    public PlaintextSetupTest() {
        log.info("--- PlaintextSetupTest ---");
    }
}

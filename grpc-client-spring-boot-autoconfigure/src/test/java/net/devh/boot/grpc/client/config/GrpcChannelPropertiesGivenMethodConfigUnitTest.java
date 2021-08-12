package net.devh.boot.grpc.client.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Title Test
 * @Description GrpcChannelPropertiesGivenMethodConfigUnitTest
 * @Program grpc-spring-boot-starter
 * @Author wushengju
 * @Version 1.0
 * @Date 2021-08-12 16:46
 * @Copyright Copyright (c) 2021 TCL Inc. All rights reserved
 **/
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "grpc.client.test.retry-enabled=true",
        "grpc.client.test.method-config[0].name[0].service=helloworld.Greeter",
        "grpc.client.test.method-config[0].name[0].method=SayHello",
        "grpc.client.test.method-config[0].retry-policy.max-attempts=2",
        "grpc.client.test.method-config[0].retry-policy.initial-backoff=1",
        "grpc.client.test.method-config[0].retry-policy.max-backoff=1",
        "grpc.client.test.method-config[0].retry-policy.backoff-multiplier=2",
        "grpc.client.test.method-config[0].retry-policy.retryable-status-codes=UNKNOWN,UNAVAILABLE"
})
public class GrpcChannelPropertiesGivenMethodConfigUnitTest {

    @Autowired
    private GrpcChannelsProperties grpcChannelsProperties;

    @Test
    void test() {
        final GrpcChannelProperties properties = this.grpcChannelsProperties.getChannel("test");
        assertEquals(true, properties.getRetryEnabled());
        assertEquals("helloworld.Greeter", properties.getMethodConfig().get(0).getName().get(0).getService());
        assertEquals("SayHello", properties.getMethodConfig().get(0).getName().get(0).getMethod());
        assertEquals(2, properties.getMethodConfig().get(0).getRetryPolicy().getMaxAttempts());
        assertEquals(1, properties.getMethodConfig().get(0).getRetryPolicy().getInitialBackoff().getSeconds());
        assertEquals(1, properties.getMethodConfig().get(0).getRetryPolicy().getMaxBackoff().getSeconds());
        assertEquals(2, properties.getMethodConfig().get(0).getRetryPolicy().getBackoffMultiplier());

        assertEquals(2, properties.getMethodConfig().get(0).getRetryPolicy().getRetryableStatusCodes().size());
    }
}

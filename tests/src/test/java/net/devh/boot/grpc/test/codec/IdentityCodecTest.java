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

package net.devh.boot.grpc.test.codec;

import java.util.Arrays;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import net.devh.boot.grpc.common.codec.GrpcCodecDefinition;
import net.devh.boot.grpc.common.codec.GrpcCodecDiscoverer;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;

/**
 * A test checking that the server and client can start and connect to each other with minimal config and no/the
 * identity codec.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@SpringBootTest(properties = "grpc.client.test.negotiationType=PLAINTEXT")
@SpringJUnitConfig(classes = {ServiceConfiguration.class, BaseAutoConfiguration.class,
        IdentityCodecTest.CustomConfiguration.class})
@DirtiesContext
public class IdentityCodecTest extends AbstractCodecTest {

    private static final String CODEC = "identity";

    public IdentityCodecTest() {
        super(CODEC);
    }

    @Configuration
    public static class CustomConfiguration {

        @Bean
        GlobalClientInterceptorConfigurer gcic() {
            return registry -> registry.addClientInterceptors(new CodecValidatingClientInterceptor(CODEC));
        }

        @Bean
        GlobalServerInterceptorConfigurer gsic() {
            return registry -> registry.addServerInterceptors(new CodecValidatingServerInterceptor(CODEC));
        }

        @Bean
        GrpcCodecDiscoverer grpcCodecDiscoverer() {
            return () -> Arrays.asList(GrpcCodecDefinition.IDENTITY_DEFINITION);
        }

    }

}

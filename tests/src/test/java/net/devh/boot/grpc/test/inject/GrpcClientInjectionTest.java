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

package net.devh.boot.grpc.test.inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.stubfactory.StandardJavaGrpcStubFactory;
import net.devh.boot.grpc.client.stubfactory.StubFactory;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.inject.CustomGrpc.ConstructorAccessibleStub;
import net.devh.boot.grpc.test.inject.CustomGrpc.CustomAccessibleStub;
import net.devh.boot.grpc.test.inject.CustomGrpc.FactoryMethodAccessibleStub;
import net.devh.boot.grpc.test.inject.GrpcClientInjectionTest.TestConfig;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

/**
 * A test checking that the client injection works.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@SpringBootTest
@SpringJUnitConfig(classes = {TestConfig.class, InProcessConfiguration.class, ServiceConfiguration.class,
        BaseAutoConfiguration.class})
@DirtiesContext
class GrpcClientInjectionTest {

    @GrpcClient("test")
    Channel channel;
    @GrpcClient("test")
    TestServiceStub stub;
    @GrpcClient("test")
    TestServiceBlockingStub blockingStub;
    @GrpcClient("test")
    TestServiceFutureStub futureStub;
    @GrpcClient("test")
    ConstructorAccessibleStub constructorStub;
    @GrpcClient("test")
    FactoryMethodAccessibleStub factoryMethodStub;
    @GrpcClient("test")
    CustomAccessibleStub customStub;

    Channel channelSetted;
    TestServiceStub stubSetted;
    TestServiceBlockingStub blockingStubSetted;
    TestServiceFutureStub futureStubSetted;
    ConstructorAccessibleStub constructorStubSetted;
    FactoryMethodAccessibleStub factoryMethodStubSetted;
    CustomAccessibleStub customStubSetted;

    @PostConstruct
    public void init() {
        // Test injection
        assertNotNull(this.channel, "channel");
        assertNotNull(this.stub, "stub");
        assertNotNull(this.blockingStub, "blockingStub");
        assertNotNull(this.futureStub, "futureStub");
        assertNotNull(this.constructorStub, "constructorStub");
        assertNotNull(this.factoryMethodStub, "factoryMethodStub");
        assertNotNull(this.customStub, "customStub");
    }

    @GrpcClient("test")
    void inject(final Channel channel) {
        assertNotNull(channel, "channel");
        this.channelSetted = channel;
    }

    @GrpcClient("test")
    void inject(final TestServiceStub stub) {
        assertNotNull(stub, "stub");
        this.stubSetted = stub;
    }

    @GrpcClient("test")
    void inject(final TestServiceBlockingStub stub) {
        assertNotNull(stub, "stub");
        this.blockingStubSetted = stub;
    }

    @GrpcClient("test")
    void inject(final TestServiceFutureStub stub) {
        assertNotNull(stub, "stub");
        this.futureStubSetted = stub;
    }

    @GrpcClient("test")
    void inject(final ConstructorAccessibleStub stub) {
        assertNotNull(stub, "stub");
        this.constructorStubSetted = stub;
    }

    @GrpcClient("test")
    void inject(final FactoryMethodAccessibleStub stub) {
        assertNotNull(stub, "stub");
        this.factoryMethodStubSetted = stub;
    }

    @GrpcClient("test")
    void inject(final CustomAccessibleStub stub) {
        assertNotNull(stub, "stub");
        this.customStubSetted = stub;
    }

    @Test
    void testAllSet() {
        // Field injection
        assertNotNull(this.channel, "channel");
        assertNotNull(this.stub, "stub");
        assertNotNull(this.blockingStub, "blockingStub");
        assertNotNull(this.futureStub, "futureStub");
        assertNotNull(this.constructorStub, "constructorStub");
        assertNotNull(this.factoryMethodStub, "factoryMethodStub");
        assertNotNull(this.customStub, "customStub");
        // Setter injection
        assertNotNull(this.channelSetted, "channelSetted");
        assertNotNull(this.stubSetted, "stubSetted");
        assertNotNull(this.blockingStubSetted, "blockingStubSetted");
        assertNotNull(this.futureStubSetted, "futureStubSetted");
        assertNotNull(this.constructorStubSetted, "constructorStubSetted");
        assertNotNull(this.factoryMethodStubSetted, "factoryMethodStubSetted");
        assertNotNull(this.customStubSetted, "customStubSetted");
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        StubFactory customStubFactory() {
            return new StandardJavaGrpcStubFactory() {

                @Override
                public boolean isApplicable(final Class<? extends AbstractStub<?>> stubType) {
                    return CustomStub.class.isAssignableFrom(stubType);
                }

                @Override
                protected String getFactoryMethodName() {
                    return "custom";
                }

            };
        }

    }

}

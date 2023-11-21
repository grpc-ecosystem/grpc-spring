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

package net.devh.boot.grpc.test.inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.stub.AbstractStub;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.stubfactory.StandardJavaGrpcStubFactory;
import net.devh.boot.grpc.client.stubfactory.StubFactory;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;


/**
 * Test case should cover auto wiring with field and method injection
 */
@SpringBootTest
@SpringJUnitConfig(
        classes = {
                GrpcClientConstructorInjectionTest.TestConfig.class,
                GrpcClientConstructorInjectionTest.GrpcClientConstructorInjectionBean.class,
                InProcessConfiguration.class,
                ServiceConfiguration.class,
                BaseAutoConfiguration.class,
        })
@DirtiesContext
class GrpcClientConstructorInjectionTest {

    @Autowired
    GrpcClientConstructorInjectionBean bean;

    @Test
    void constructorInjectTest() {
        assertNotNull(bean.blockingStub, "grpc client constructor injection");
    }

    @Component
    public static class GrpcClientConstructorInjectionBean {
        public TestServiceGrpc.TestServiceBlockingStub blockingStub;
        public TestServiceGrpc.TestServiceFutureStub futureStubForClientTest;
        public TestServiceGrpc.TestServiceBlockingStub anotherBlockingStub;
        public TestServiceGrpc.TestServiceBlockingStub unnamedTestServiceBlockingStub;
        public CustomGrpc.FactoryMethodAccessibleStub anotherServiceClientBean;

        public GrpcClientConstructorInjectionBean(
                @GrpcClient("test") TestServiceGrpc.TestServiceBlockingStub blockingStub,
                @GrpcClient("test") TestServiceGrpc.TestServiceFutureStub futureStubForClientTest,
                @GrpcClient("anotherTest") TestServiceGrpc.TestServiceBlockingStub anotherBlockingStub,
                @GrpcClient("unnamed") TestServiceGrpc.TestServiceBlockingStub unnamedTestServiceBlockingStub,
                @GrpcClient("test") CustomGrpc.FactoryMethodAccessibleStub anotherServiceClientBean) {
            this.blockingStub = blockingStub;
            this.futureStubForClientTest = futureStubForClientTest;
            this.anotherBlockingStub = anotherBlockingStub;
            this.unnamedTestServiceBlockingStub = unnamedTestServiceBlockingStub;
            this.anotherServiceClientBean = anotherServiceClientBean;
        }
    }

    @Configuration
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

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.stub.AbstractStub;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
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
                GrpcClientBeanInjectionTest.TestConfig.class,
                GrpcClientBeanInjectionTest.CustomConfig.class,
                InProcessConfiguration.class,
                ServiceConfiguration.class,
                BaseAutoConfiguration.class
        })
@DirtiesContext
class GrpcClientBeanInjectionTest {

    @Autowired
    TestServiceGrpc.TestServiceBlockingStub blockingStub;

    @Autowired
    TestServiceGrpc.TestServiceFutureStub futureStubForClientTest;

    @Autowired
    TestServiceGrpc.TestServiceBlockingStub anotherBlockingStub;

    @Autowired
    TestServiceGrpc.TestServiceBlockingStub unnamedTestServiceBlockingStub;

    @Autowired
    CustomGrpc.FactoryMethodAccessibleStub anotherServiceClientBean;

    @Autowired
    String aboutMethodInjectedBlockingStubBean;

    @Autowired
    TestServiceGrpc.TestServiceBlockingStub stubFromSpringConfiguration;

    /**
     * Test should cover bean simple single bean creation with {@link GrpcClientBean} annotation with
     * {@link TestConfiguration}
     */
    @Test
    void singleContextInjectionFromTestConfigurationTest() {
        assertNotNull(this.blockingStub, "blockingStub");
    }

    /**
     * Test should cover bean simple single bean creation with {@link GrpcClientBean} annotation with
     * {@link Configuration}
     */
    @Test
    void singleContextInjectionFromConfigurationTest() {
        assertNotNull(this.stubFromSpringConfiguration, "stubFromSpringConfiguration");
    }

    /**
     * Test should cover creation of another bean with different stub class and same grpc client definition
     */
    @Test
    void anotherSubTypeAndSameClientDefinitionTest() {
        assertNotNull(this.futureStubForClientTest, "futureStubForClientTest");
    }

    /**
     * Test should cover creation of another bean same different stub class, but different grpc client definition
     */
    @Test
    void twoDifferentClientDefinitionsTest() {
        assertNotNull(this.anotherBlockingStub, "blockingStub");
    }

    /**
     * Test should cover creation of another bean with different service and stub class with same grpc client definition
     */
    @Test
    void anotherGrpcServiceAndSameGrpcClientDefinitionTest() {
        assertNotNull(this.anotherServiceClientBean, "anotherServiceClientBean");
    }

    /**
     * Test should cover creation of bean without defined bean name
     */
    @Test
    void unnamedBeanContextInjectionTest() {
        assertNotNull(this.unnamedTestServiceBlockingStub, "unnamedTestServiceBlockingStub");
    }

    /**
     * Test should cover bean method injection via {@link Autowired} and {@link Qualifier} from context
     */
    @Test
    void autoWiringQualifierMethodInjectionFromContextTest() {
        assertNotNull(this.aboutMethodInjectedBlockingStubBean, "aboutBlockingStubBean");
    }

    @TestConfiguration
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceBlockingStub.class,
            beanName = "blockingStub",
            client = @GrpcClient("test"))
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceFutureStub.class,
            beanName = "futureStubForClientTest",
            client = @GrpcClient("test"))
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceBlockingStub.class,
            beanName = "anotherBlockingStub",
            client = @GrpcClient("anotherTest"))
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceBlockingStub.class,
            client = @GrpcClient("unnamed"))
    @GrpcClientBean(
            clazz = CustomGrpc.FactoryMethodAccessibleStub.class,
            beanName = "anotherServiceClientBean",
            client = @GrpcClient("test"))
    public static class TestConfig {

        @Bean
        String aboutMethodInjectedBlockingStubBean(
                @Qualifier("anotherBlockingStub") final TestServiceGrpc.TestServiceBlockingStub blockingStub) {
            return blockingStub.toString();
        }

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

    @Configuration
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceBlockingStub.class,
            beanName = "stubFromSpringConfiguration",
            client = @GrpcClient("test2"))
    public static class CustomConfig {
    }

}

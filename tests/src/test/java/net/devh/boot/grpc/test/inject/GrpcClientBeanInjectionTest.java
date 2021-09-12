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
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import net.devh.boot.grpc.client.inject.GrpcClientBeans;
import net.devh.boot.grpc.client.stubfactory.StandardJavaGrpcStubFactory;
import net.devh.boot.grpc.client.stubfactory.StubFactory;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

/**
 * Test case should cover auto wiring with field and method injection
 */
@Slf4j
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
public class GrpcClientBeanInjectionTest {

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
        assertNotNull(blockingStub, "blockingStub");
    }

    /**
     * Test should cover bean simple single bean creation with {@link GrpcClientBean} annotation with
     * {@link Configuration}
     */
    @Test
    void singleContextInjectionFromConfigurationTest() {
        assertNotNull(stubFromSpringConfiguration, "stubFromSpringConfiguration");
    }

    /**
     * Test should cover creation of another bean with different stub class and same grpc client definition
     */
    @Test
    void anotherSubTypeAndSameClientDefinitionTest() {
        assertNotNull(futureStubForClientTest, "futureStubForClientTest");
    }

    /**
     * Test should cover creation of another bean same different stub class, but different grpc client definition
     */
    @Test
    void twoDifferentClientDefinitionsTest() {
        assertNotNull(anotherBlockingStub, "blockingStub");
    }

    /**
     * Test should cover creation of another bean with different service and stub class with same grpc client definition
     */
    @Test
    void anotherGrpcServiceAndSameGrpcClientDefinitionTest() {
        assertNotNull(anotherServiceClientBean, "anotherServiceClientBean");
    }

    /**
     * Test should cover creation of bean without defined bean name
     */
    @Test
    void unnamedBeanContextInjectionTest() {
        assertNotNull(unnamedTestServiceBlockingStub, "unnamedTestServiceBlockingStub");
    }

    /**
     * Test should cover bean method injection via {@link Autowired} and {@link Qualifier} from context
     */
    @Test
    void autoWiringQualifierMethodInjectionFromContextTest() {
        assertNotNull(aboutMethodInjectedBlockingStubBean, "aboutBlockingStubBean");
    }

    @TestConfiguration
    @GrpcClientBeans(value = {
            @GrpcClientBean(
                    clazz = TestServiceGrpc.TestServiceBlockingStub.class,
                    beanName = "blockingStub",
                    client = @GrpcClient("test")),
            @GrpcClientBean(
                    clazz = TestServiceGrpc.TestServiceFutureStub.class,
                    beanName = "futureStubForClientTest",
                    client = @GrpcClient("test")),
            @GrpcClientBean(
                    clazz = TestServiceGrpc.TestServiceBlockingStub.class,
                    beanName = "anotherBlockingStub",
                    client = @GrpcClient("anotherTest")),
            @GrpcClientBean(
                    clazz = TestServiceGrpc.TestServiceBlockingStub.class,
                    client = @GrpcClient("unnamed")),
            @GrpcClientBean(
                    clazz = CustomGrpc.FactoryMethodAccessibleStub.class,
                    beanName = "anotherServiceClientBean",
                    client = @GrpcClient("test"))
    })
    public static class TestConfig {

        @Bean
        public String aboutMethodInjectedBlockingStubBean(
                @Autowired
                @Qualifier("anotherBlockingStub") TestServiceGrpc.TestServiceBlockingStub blockingStub) {
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

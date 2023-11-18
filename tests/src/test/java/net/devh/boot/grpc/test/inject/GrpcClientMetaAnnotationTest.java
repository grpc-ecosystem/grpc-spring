/*
 * Copyright (c) 2016-2021 The gRPC-Spring Authors
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.stub.AbstractStub;
import net.devh.boot.grpc.client.stubfactory.StandardJavaGrpcStubFactory;
import net.devh.boot.grpc.client.stubfactory.StubFactory;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.inject.CustomGrpc.ConstructorAccessibleStub;
import net.devh.boot.grpc.test.inject.CustomGrpc.CustomAccessibleStub;
import net.devh.boot.grpc.test.inject.CustomGrpc.FactoryMethodAccessibleStub;
import net.devh.boot.grpc.test.inject.GrpcClientInjectionTest.TestConfig;
import net.devh.boot.grpc.test.inject.metaannotation.GrpcClientWrapper;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceBlockingStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceFutureStub;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceStub;

/**
 * A test checking that the client injection works.
 *
 * @author Hemant Vyas (v313hemant@gmail.com)
 */
@SpringBootTest
@SpringJUnitConfig(classes = {TestConfig.class, InProcessConfiguration.class,
        ServiceConfiguration.class,
        BaseAutoConfiguration.class})
@DirtiesContext
class GrpcClientMetaAnnotationTest {

    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    TestServiceStub stub;
    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    TestServiceBlockingStub blockingStub;
    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    TestServiceFutureStub futureStub;
    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    ConstructorAccessibleStub constructorStub;
    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    FactoryMethodAccessibleStub factoryMethodStub;
    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    CustomAccessibleStub customStub;

    TestServiceStub stubSetted;
    TestServiceBlockingStub blockingStubSetted;
    TestServiceFutureStub futureStubSetted;
    ConstructorAccessibleStub constructorStubSetted;
    FactoryMethodAccessibleStub factoryMethodStubSetted;
    CustomAccessibleStub customStubSetted;

    @PostConstruct
    public void init() {
        // Test injection
        assertNotNull(this.stub, "stub");
        assertNotNull(this.blockingStub, "blockingStub");
        assertNotNull(this.futureStub, "futureStub");
        assertNotNull(this.constructorStub, "constructorStub");
        assertNotNull(this.factoryMethodStub, "factoryMethodStub");
        assertNotNull(this.customStub, "customStub");

        assertAnnotationExtraParams("testExtraParamString", true);
    }

    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    void inject(final TestServiceStub stub) {
        assertNotNull(stub, "stub");
        this.stubSetted = stub;
    }

    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    void inject(final TestServiceBlockingStub stub) {
        assertNotNull(stub, "stub");
        this.blockingStubSetted = stub;
    }

    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    void inject(final TestServiceFutureStub stub) {
        assertNotNull(stub, "stub");
        this.futureStubSetted = stub;
    }

    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    void inject(final ConstructorAccessibleStub stub) {
        assertNotNull(stub, "stub");
        this.constructorStubSetted = stub;
    }

    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    void inject(final FactoryMethodAccessibleStub stub) {
        assertNotNull(stub, "stub");
        this.factoryMethodStubSetted = stub;
    }

    @GrpcClientWrapper(value = "test", extraParamString = "testExtraParamString", extraParamBoolean = true)
    void inject(final CustomAccessibleStub stub) {
        assertNotNull(stub, "stub");
        this.customStubSetted = stub;
    }

    @Test
    void testAllSet() {
        // Field injection
        assertNotNull(this.stub, "stub");
        assertNotNull(this.blockingStub, "blockingStub");
        assertNotNull(this.futureStub, "futureStub");
        assertNotNull(this.constructorStub, "constructorStub");
        assertNotNull(this.factoryMethodStub, "factoryMethodStub");
        assertNotNull(this.customStub, "customStub");
        // Setter injection
        assertNotNull(this.stubSetted, "stubSetted");
        assertNotNull(this.blockingStubSetted, "blockingStubSetted");
        assertNotNull(this.futureStubSetted, "futureStubSetted");
        assertNotNull(this.constructorStubSetted, "constructorStubSetted");
        assertNotNull(this.factoryMethodStubSetted, "factoryMethodStubSetted");
        assertNotNull(this.customStubSetted, "customStubSetted");
    }

    @Test
    void AnnotationExtraParamsNegativeTest() {

        AssertionFailedError exceptionA = assertThrows(
                AssertionFailedError.class,
                () -> assertAnnotationExtraParams("INCORRECT_VALUE", true),
                "failed");

        AssertionFailedError exceptionB = assertThrows(
                AssertionFailedError.class,
                () -> assertAnnotationExtraParams("testExtraParamString", false),
                "failed");

        assertNotNull(exceptionA);
        assertNotNull(exceptionB);
    }

    private void assertAnnotationExtraParams(String extraParamStringValue,
            boolean extraParamBooleanValue) {
        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field field : fields) {
            if (field.isAnnotationPresent(GrpcClientWrapper.class)) {
                final GrpcClientWrapper annotation = field.getAnnotation(GrpcClientWrapper.class);
                assertEquals(extraParamStringValue, annotation.extraParamString());
                assertEquals(extraParamBooleanValue, annotation.extraParamBoolean());
            }
        }
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

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

/**
 * Test case covering probable conflicting situations with {@link GrpcClientBean} and {@link GrpcClient} usage.
 */
class GrpcClientBeanInjectionNegativeTest {

    @Test
    void duplicateGrpcClientBeansTest() {
        final SpringApplication app = new SpringApplication(
                TwoSameGrpcClientBeansConfig.class,
                InProcessConfiguration.class,
                ServiceConfiguration.class,
                BaseAutoConfiguration.class);

        final BeanCreationException error = assertThrows(BeanCreationException.class, app::run);

        final BeanCreationException cause = (BeanCreationException) error.getCause();
        assertEquals("duplicateStub", cause.getBeanName());
        assertThat(cause).hasMessageContaining(TwoSameGrpcClientBeansConfig.class.getName());
    }

    @Test
    void badGrpcClientBeanTest() {
        final SpringApplication app = new SpringApplication(
                BadGrpcClientBeanConfig.class,
                InProcessConfiguration.class,
                ServiceConfiguration.class,
                BaseAutoConfiguration.class);

        final BeanCreationException error = assertThrows(BeanCreationException.class, app::run);

        final BeanCreationException cause = (BeanCreationException) error.getCause();
        assertEquals("badStub", cause.getBeanName());
        assertThat(cause)
                .hasMessageContaining(BadGrpcClientBeanConfig.class.getName())
                .hasMessageContaining(String.class.getName());
    }

    @Test
    @Disabled("This does not fail unexpectedly")
    void mixedGrpcClientBeanAndFieldTest() {
        final SpringApplication app = new SpringApplication(
                MixedBeanConfig.class,
                InProcessConfiguration.class,
                ServiceConfiguration.class,
                BaseAutoConfiguration.class);

        assertThrows(BeanCreationException.class, app::run);
    }

    @TestConfiguration
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceBlockingStub.class,
            beanName = "duplicateStub",
            client = @GrpcClient("test"))
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceBlockingStub.class,
            beanName = "duplicateStub",
            client = @GrpcClient("test"))
    public static class TwoSameGrpcClientBeansConfig {
    }

    @TestConfiguration
    @GrpcClientBean(
            clazz = String.class,
            beanName = "badStub",
            client = @GrpcClient("test"))
    public static class BadGrpcClientBeanConfig {
    }

    @TestConfiguration
    @GrpcClientBean(
            clazz = TestServiceGrpc.TestServiceFutureStub.class,
            beanName = "mixedStub",
            client = @GrpcClient("test"))
    public static class MixedBeanConfig {

        @GrpcClient("test")
        TestServiceGrpc.TestServiceBlockingStub stub;

        @Bean("mixedStub")
        public TestServiceGrpc.TestServiceBlockingStub mixedStub() {
            return this.stub;
        }

    }

}

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

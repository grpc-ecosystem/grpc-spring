/*
 * Copyright (c) 2016-2023 Michael Zhang <yidongnan@gmail.com>
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.stub.AbstractStub;
import io.micrometer.core.instrument.MeterRegistry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.stubfactory.StandardJavaGrpcStubFactory;
import net.devh.boot.grpc.client.stubfactory.StubFactory;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.InProcessConfiguration;
import net.devh.boot.grpc.test.config.MetricConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

@SpringBootTest
@SpringJUnitConfig(
        classes = {
                GrpcClientMetricsTest.TestConfig.class,
                GrpcClientMetricsTest.GrpcClientConstructorInjectionBean.class,
                InProcessConfiguration.class,
                ServiceConfiguration.class,
                BaseAutoConfiguration.class,
                MetricConfiguration.class
        })
@DirtiesContext
public class GrpcClientMetricsTest {
    @Autowired
    MeterRegistry registry;

    @Test
    void jvmMetricsTest() {
        assertThat(registry.getMeters())
                .filteredOn(meter -> meter.getId().getName().contains("jvm"))
                .isNotEmpty();
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
    @ImportAutoConfiguration({MetricsAutoConfiguration.class, JvmMetricsAutoConfiguration.class})
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

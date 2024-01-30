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

package net.devh.boot.grpc.test.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.ClientInterceptor;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingClientInterceptor;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcClientInterceptor;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.client.metrics.MetricsClientInterceptor;

@SpringBootTest
@SpringJUnitConfig(classes = {GrpcClientAutoConfiguration.class})
@EnableAutoConfiguration
@DirtiesContext
public class DefaultClientInterceptorTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GlobalClientInterceptorRegistry registry;

    @Test
    void testDefaultInterceptors() {
        final List<ClientInterceptor> expected = new ArrayList<>();
        expected.add(this.applicationContext.getBean(MetricCollectingClientInterceptor.class));
        expected.add(this.applicationContext.getBean(MetricsClientInterceptor.class));
        expected.add(this.applicationContext.getBean(ObservationGrpcClientInterceptor.class));

        final List<ClientInterceptor> actual = new ArrayList<>(this.registry.getClientInterceptors());
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}

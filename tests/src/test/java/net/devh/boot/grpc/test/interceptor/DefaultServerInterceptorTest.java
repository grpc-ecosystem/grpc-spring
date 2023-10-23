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

import static net.devh.boot.grpc.common.util.InterceptorOrder.beanFactoryAwareOrderComparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import net.devh.boot.grpc.server.scope.GrpcRequestScope;
import net.devh.boot.grpc.server.security.interceptors.AuthenticatingServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.AuthorizationCheckingServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.ExceptionTranslatingServerInterceptor;
import net.devh.boot.grpc.test.config.ManualSecurityConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.config.WithBasicAuthSecurityConfiguration;

@SpringBootTest
@SpringJUnitConfig(classes = {
        ServiceConfiguration.class,
        WithBasicAuthSecurityConfiguration.class,
        ManualSecurityConfiguration.class})
@EnableAutoConfiguration
@DirtiesContext
class DefaultServerInterceptorTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GlobalServerInterceptorRegistry registry;

    @Test
    void testOrderingOfTheDefaultInterceptors() {
        final List<ServerInterceptor> expected = new ArrayList<>();
        expected.add(this.applicationContext.getBean(GrpcRequestScope.class));
        expected.add(this.applicationContext.getBean(MetricCollectingServerInterceptor.class));
        expected.add(this.applicationContext.getBean(ObservationGrpcServerInterceptor.class));
        expected.add(this.applicationContext.getBean(ExceptionTranslatingServerInterceptor.class));
        expected.add(this.applicationContext.getBean(AuthenticatingServerInterceptor.class));
        expected.add(this.applicationContext.getBean(AuthorizationCheckingServerInterceptor.class));

        final List<ServerInterceptor> actual = new ArrayList<>(this.registry.getServerInterceptors());
        assertEquals(expected, actual);

        Collections.shuffle(actual);
        actual.sort(beanFactoryAwareOrderComparator(this.applicationContext, ServerInterceptor.class));
        assertEquals(expected, actual);
    }

}

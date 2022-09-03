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

package net.devh.boot.grpc.test.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import net.devh.boot.grpc.server.scope.GrpcRequestScope;
import net.devh.boot.grpc.server.security.interceptors.AuthenticatingServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.AuthorizationCheckingServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.ExceptionTranslatingServerInterceptor;
import net.devh.boot.grpc.test.config.ManualSecurityConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;
import net.devh.boot.grpc.test.config.WithBasicAuthSecurityConfiguration;

@SpringBootTest
@SpringJUnitConfig(classes = {ServiceConfiguration.class, WithBasicAuthSecurityConfiguration.class,
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
        List<ServerInterceptor> expected = new ArrayList<>();
        expected.add(this.applicationContext.getBean(GrpcRequestScope.class));
        expected.add(this.applicationContext.getBean(MetricCollectingServerInterceptor.class));
        expected.add(this.applicationContext.getBean(ExceptionTranslatingServerInterceptor.class));
        expected.add(this.applicationContext.getBean(AuthenticatingServerInterceptor.class));
        expected.add(this.applicationContext.getBean(AuthorizationCheckingServerInterceptor.class));

        List<ServerInterceptor> actual = new ArrayList<>(this.registry.getServerInterceptors());
        assertEquals(expected, actual);

        Collections.shuffle(actual);
        AnnotationAwareOrderComparator.sort(actual);
        assertEquals(expected, actual);
    }

}

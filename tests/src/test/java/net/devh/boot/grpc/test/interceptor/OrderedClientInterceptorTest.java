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

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.common.collect.Iterators;

import io.grpc.ClientInterceptor;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.test.config.OrderedClientInterceptorConfiguration;

@SpringBootTest
@SpringJUnitConfig(classes = {OrderedClientInterceptorConfiguration.class, GrpcClientAutoConfiguration.class})
@DirtiesContext
class OrderedClientInterceptorTest {

    @Autowired
    GlobalClientInterceptorRegistry registry;

    @Test
    void testOrderingByOrderAnnotation() {
        final int firstInterceptorIndex = findIndexOfClass(this.registry.getClientInterceptors(),
                OrderedClientInterceptorConfiguration.FirstOrderAnnotatedInterceptor.class);
        final int secondInterceptorIndex = findIndexOfClass(this.registry.getClientInterceptors(),
                OrderedClientInterceptorConfiguration.SecondOrderAnnotatedInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    void testOrderingByPriorityAnnotation() {
        final int firstInterceptorIndex = findIndexOfClass(this.registry.getClientInterceptors(),
                OrderedClientInterceptorConfiguration.FirstPriorityAnnotatedInterceptor.class);
        final int secondInterceptorIndex = findIndexOfClass(this.registry.getClientInterceptors(),
                OrderedClientInterceptorConfiguration.SecondPriorityAnnotatedInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    void testOrderingByOrderedInterface() {
        final int firstInterceptorIndex = findIndexOfClass(this.registry.getClientInterceptors(),
                OrderedClientInterceptorConfiguration.FirstOrderedInterfaceInterceptor.class);
        final int secondInterceptorIndex = findIndexOfClass(this.registry.getClientInterceptors(),
                OrderedClientInterceptorConfiguration.SecondOrderedInterfaceInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    void testOrderingByOrderedBean() {
        final int firstInterceptorIndex =
                findIndexOfName(this.registry.getClientInterceptors(), "firstOrderAnnotationInterceptorBean");
        final int secondInterceptorIndex =
                findIndexOfName(this.registry.getClientInterceptors(), "secondOrderAnnotationInterceptorBean");
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    private int findIndexOfClass(final List<ClientInterceptor> interceptors, final Class<?> clazz) {
        return Iterators.indexOf(interceptors.iterator(), clazz::isInstance);
    }

    private int findIndexOfName(final List<ClientInterceptor> interceptors, final String name) {
        return Iterators.indexOf(interceptors.iterator(), bean -> bean.toString().equals(name));
    }

}

/*
 * Copyright (c) 2016-2022 Michael Zhang <yidongnan@gmail.com>
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

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.common.collect.Iterators;

import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import net.devh.boot.grpc.test.config.OrderedServerInterceptorConfiguration;

@SpringBootTest
@SpringJUnitConfig(classes = {OrderedServerInterceptorConfiguration.class, GrpcServerAutoConfiguration.class})
@DirtiesContext
class OrderedServerInterceptorTest {

    @Autowired
    GlobalServerInterceptorRegistry registry;

    @Test
    void testOrderingByOrderAnnotation() {
        final int firstInterceptorIndex = findIndexOfClass(this.registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.FirstOrderAnnotatedInterceptor.class);
        final int secondInterceptorIndex = findIndexOfClass(this.registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.SecondOrderAnnotatedInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    void testOrderingByPriorityAnnotation() {
        final int firstInterceptorIndex = findIndexOfClass(this.registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.FirstPriorityAnnotatedInterceptor.class);
        final int secondInterceptorIndex = findIndexOfClass(this.registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.SecondPriorityAnnotatedInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    void testOrderingByOrderedInterface() {
        final int firstInterceptorIndex = findIndexOfClass(this.registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.FirstOrderedInterfaceInterceptor.class);
        final int secondInterceptorIndex = findIndexOfClass(this.registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.SecondOrderedInterfaceInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    void testOrderingByOrderedBean() {
        final int firstInterceptorIndex =
                findIndexOfName(this.registry.getServerInterceptors(), "firstOrderAnnotationInterceptorBean");
        final int secondInterceptorIndex =
                findIndexOfName(this.registry.getServerInterceptors(), "secondOrderAnnotationInterceptorBean");
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    private int findIndexOfClass(final List<ServerInterceptor> interceptors, final Class<?> clazz) {
        return Iterators.indexOf(interceptors.iterator(), clazz::isInstance);
    }

    private int findIndexOfName(final List<ServerInterceptor> interceptors, final String name) {
        return Iterators.indexOf(interceptors.iterator(), bean -> bean.toString().equals(name));
    }

}

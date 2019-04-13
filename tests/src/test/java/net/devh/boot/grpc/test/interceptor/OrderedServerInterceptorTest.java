/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import net.devh.boot.grpc.test.config.OrderedServerInterceptorConfiguration;

@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = {OrderedServerInterceptorConfiguration.class, GrpcServerAutoConfiguration.class})
@DirtiesContext
public class OrderedServerInterceptorTest {

    @Autowired
    GlobalServerInterceptorRegistry registry;

    @Test
    public void testOrderingByOrderAnnotation() {
        int firstInterceptorIndex = findIndexOfClass(registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.FirstOrderAnnotatedInterceptor.class);
        int secondInterceptorIndex = findIndexOfClass(registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.SecondOrderAnnotatedInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    public void testOrderingByPriorityAnnotation() {
        int firstInterceptorIndex = findIndexOfClass(registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.FirstPriorityAnnotatedInterceptor.class);
        int secondInterceptorIndex = findIndexOfClass(registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.SecondPriorityAnnotatedInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    @Test
    public void testOrderingByOrderedInterface() {
        int firstInterceptorIndex = findIndexOfClass(registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.FirstOrderedInterfaceInterceptor.class);
        int secondInterceptorIndex = findIndexOfClass(registry.getServerInterceptors(),
                OrderedServerInterceptorConfiguration.SecondOrderedInterfaceInterceptor.class);
        Assert.assertTrue(firstInterceptorIndex < secondInterceptorIndex);
    }

    private int findIndexOfClass(List<ServerInterceptor> interceptors, Class clazz) {
        return Iterators.indexOf(interceptors.iterator(), clazz::isInstance);
    }
}

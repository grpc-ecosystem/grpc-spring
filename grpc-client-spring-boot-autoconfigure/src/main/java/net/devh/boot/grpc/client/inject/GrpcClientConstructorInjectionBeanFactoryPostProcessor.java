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

package net.devh.boot.grpc.client.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class GrpcClientConstructorInjectionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

        GrpcClientConstructorInjection grpcClientConstructorInjection = new GrpcClientConstructorInjection();

        // Use bean name to get bean class to avoid triggering bean init
        beanFactory.getBeanNamesIterator().forEachRemaining(beanName -> {
            Class<?> clazz = beanFactory.getType(beanName);
            if (clazz == null) {
                return;
            }

            // Search for GrpcClient annotation in all parameters of all constructors
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                for (Parameter parameter : constructor.getParameters()) {
                    GrpcClient client = parameter.getAnnotation(GrpcClient.class);
                    if (client == null) {
                        continue;
                    }
                    GrpcClientConstructorInjection.GrpcClientBeanInjection injection =
                            new GrpcClientConstructorInjection.GrpcClientBeanInjection(
                                    parameter.getType(),
                                    client,
                                    clazz);
                    grpcClientConstructorInjection.add(injection);
                }
            }
        });

        beanFactory.registerSingleton("grpcClientInjects", grpcClientConstructorInjection);
    }
}

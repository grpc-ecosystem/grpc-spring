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

import static net.devh.boot.grpc.client.inject.GrpcClientConstructorInjection.BEAN_NAME;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * {@link BeanFactoryPostProcessor} that searches the bean definitions for {@link GrpcClient} annotations on
 * constructors and factory methods.
 */
public class GrpcClientConstructorInjectionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@NotNull final ConfigurableListableBeanFactory beanFactory)
            throws BeansException {

        final GrpcClientConstructorInjection grpcClientConstructorInjection = new GrpcClientConstructorInjection();

        // Use bean name to get bean class to avoid triggering bean init
        beanFactory.getBeanNamesIterator().forEachRemaining(beanName -> {
            final Class<?> clazz = beanFactory.getType(beanName);
            if (clazz == null) {
                return;
            }

            BeanDefinition beanDefinition = null;
            try {
                beanDefinition = beanFactory.getBeanDefinition(beanName);
            } catch (final NoSuchBeanDefinitionException ignored) {
                return;
            }

            // Search for GrpcClient annotation in all parameters of all constructors
            for (final Executable constructor : getConstructorCandidates(beanFactory, beanDefinition, clazz)) {
                final Parameter[] parameters = constructor.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    final Parameter parameter = parameters[i];
                    final GrpcClient client = parameter.getAnnotation(GrpcClient.class);
                    if (client == null) {
                        continue;
                    }

                    final GrpcClientConstructorInjection.Registry registry =
                            new GrpcClientConstructorInjection.Registry(
                                    parameter.getType(),
                                    client,
                                    clazz,
                                    beanDefinition,
                                    i);

                    grpcClientConstructorInjection.add(registry);
                }
            }
        });

        if (!grpcClientConstructorInjection.isEmpty()) {
            beanFactory.registerSingleton(BEAN_NAME, grpcClientConstructorInjection);
        }
    }

    private Executable[] getConstructorCandidates(
            final ConfigurableListableBeanFactory beanFactory,
            final BeanDefinition beanDefinition,
            final Class<?> clazz) {

        if (beanDefinition != null) {
            final String factoryBeanName = beanDefinition.getFactoryBeanName();
            final String factoryMethodName = beanDefinition.getFactoryMethodName();
            if (factoryBeanName != null && factoryMethodName != null) {
                final Class<?> factoryClass = beanFactory.getType(factoryBeanName);
                if (factoryClass != null) {
                    return Arrays.stream(factoryClass.getDeclaredMethods())
                            .filter(m -> factoryMethodName.equals(m.getName()))
                            .toArray(Executable[]::new);
                }
            }
        }
        return clazz.getDeclaredConstructors();
    }

}

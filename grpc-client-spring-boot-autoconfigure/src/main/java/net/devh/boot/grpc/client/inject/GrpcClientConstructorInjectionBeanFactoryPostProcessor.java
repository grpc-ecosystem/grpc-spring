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

package net.devh.boot.grpc.client.inject;

import static net.devh.boot.grpc.client.inject.GrpcClientConstructorInjection.BEAN_NAME;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import jakarta.validation.constraints.NotNull;

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

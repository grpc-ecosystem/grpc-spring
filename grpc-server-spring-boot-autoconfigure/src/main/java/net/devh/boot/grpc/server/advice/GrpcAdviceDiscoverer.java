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

package net.devh.boot.grpc.server.advice;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils.MethodFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * A discovery class to find all Beans annotated with {@link GrpcAdvice @GrpcAdvice} and for all found beans a second
 * search is performed looking for methods with {@link GrpcExceptionHandler @GrpcExceptionHandler}.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcAdvice
 * @see GrpcExceptionHandler
 */
@Slf4j
public class GrpcAdviceDiscoverer implements InitializingBean, ApplicationContextAware {

    /**
     * A filter for selecting {@code @GrpcExceptionHandler} methods.
     */
    public static final MethodFilter EXCEPTION_HANDLER_METHODS =
            method -> AnnotatedElementUtils.hasAnnotation(method, GrpcExceptionHandler.class);

    private ApplicationContext applicationContext;
    private Map<String, Object> annotatedBeans;
    private Set<Method> annotatedMethods;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        annotatedBeans = applicationContext.getBeansWithAnnotation(GrpcAdvice.class);
        annotatedBeans.forEach(
                (key, value) -> log.debug("Found gRPC advice: " + key + ", class: " + value.getClass().getName()));

        annotatedMethods = findAnnotatedMethods();
    }

    private Set<Method> findAnnotatedMethods() {
        return this.annotatedBeans.values().stream()
                .map(Object::getClass)
                .map(this::findAnnotatedMethods)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<Method> findAnnotatedMethods(final Class<?> clazz) {
        return MethodIntrospector.selectMethods(clazz, EXCEPTION_HANDLER_METHODS);
    }

    public Map<String, Object> getAnnotatedBeans() {
        Assert.state(annotatedBeans != null, "@GrpcAdvice annotation scanning failed.");
        return annotatedBeans;
    }

    public Set<Method> getAnnotatedMethods() {
        Assert.state(annotatedMethods != null, "@GrpcExceptionHandler annotation scanning failed.");
        return annotatedMethods;
    }

}

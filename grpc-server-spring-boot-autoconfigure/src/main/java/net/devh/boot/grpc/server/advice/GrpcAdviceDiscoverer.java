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

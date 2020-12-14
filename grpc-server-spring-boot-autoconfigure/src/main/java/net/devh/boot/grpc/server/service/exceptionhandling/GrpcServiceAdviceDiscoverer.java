/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.service.exceptionhandling;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.extern.slf4j.Slf4j;

/**
 * A discovery class to find all Beans annotated with {@link GrpcServiceAdvice @GrpcServiceAdvice} and for all found
 * beans a second search is performed looking for methods with {@link GrpcExceptionHandler @GrpcExceptionHandler}.<br>
 * <br>
 * 
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcServiceAdvice
 * @see GrpcExceptionHandler
 */
@Slf4j
public class GrpcServiceAdviceDiscoverer implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Map<String, Object> annotatedBeans;
    private Set<Class<?>> annotatedClasses;
    private Set<Method> annotatedMethods;


    Map<String, Object> getAnnotatedBeans() {
        return annotatedBeans;
    }

    Set<Method> getAnnotatedMethods() {
        return annotatedMethods;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        annotatedBeans = applicationContext.getBeansWithAnnotation(GrpcServiceAdvice.class);
        annotatedClasses = findAllAnnotatedClasses();
        annotatedMethods = getAnnotatedMethods(annotatedClasses);
    }

    private Set<Class<?>> findAllAnnotatedClasses() {
        return annotatedBeans.values()
                .stream()
                .map(Object::getClass)
                .collect(Collectors.toSet());
    }

    private Set<Method> getAnnotatedMethods(Set<Class<?>> annotatedClasses) {
        Function<Class<?>, Stream<Method>> extractMethodsFromClass = clazz -> Arrays.stream(clazz.getDeclaredMethods());
        return annotatedClasses.stream()
                .flatMap(extractMethodsFromClass)
                .filter(method -> method.isAnnotationPresent(GrpcExceptionHandler.class))
                .collect(Collectors.toSet());
    }

    boolean isAnnotationPresent() {

        return !annotatedClasses.isEmpty() && !annotatedMethods.isEmpty();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}

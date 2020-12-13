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
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO..
 * 
 * @author Andjelko (andjelko.perisic@gmail.com)
 */
@Slf4j
@Component
@ConditionalOnBean(annotation = GrpcServiceAdvice.class)
class GrpcExceptionHandlerMethodResolver implements InitializingBean {

    private final Map<Class<? extends Throwable>, Method> mappedMethods = new HashMap<>(16);
    private final ApplicationContext applicationContext;

    private Class<? extends Throwable>[] annotatedExceptions;
    private Map<String, Object> annotatedBeans;


    GrpcExceptionHandlerMethodResolver(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Set<Class<?>> annotatedClasses = findAllAnnotatedClasses();
        Set<Method> annotatedMethods = getAnnotatedMethods(annotatedClasses);

        annotatedMethods.forEach(this::checkParamTypesAreAlignedToExceptionTypes);
    }

    private Set<Class<?>> findAllAnnotatedClasses() {
        annotatedBeans = applicationContext.getBeansWithAnnotation(GrpcServiceAdvice.class);
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


    private void checkParamTypesAreAlignedToExceptionTypes(Method method) {

        GrpcExceptionHandler annotation = method.getDeclaredAnnotation(GrpcExceptionHandler.class);
        Assert.notNull(annotation, "@GrpcExceptionHandler annotation not found.");
        annotatedExceptions = annotation.value();
        Class<? extends Throwable>[] methodParamTypes = checkForExceptionType(method.getParameterTypes());

        Class<? extends Throwable>[] exceptionTypesToAdd =
                methodParamTypes.length > 0 ? methodParamTypes : annotatedExceptions;

        Arrays.stream(exceptionTypesToAdd).forEach(exceptionType -> addExceptionMapping(exceptionType, method));
    }

    private Class<? extends Throwable>[] checkForExceptionType(Class<?>[] methodParamTypes) {

        for (Class<?> methodParamType : methodParamTypes) {
            if (!Throwable.class.isAssignableFrom(methodParamType)) {
                throw new IllegalStateException("Annotated Class is not of Type Throwable: " + methodParamType);
            }
        }
        // safe to call, prior to the check above
        @SuppressWarnings("unchecked")
        Class<? extends Throwable>[] paramExceptionTypes = (Class<? extends Throwable>[]) methodParamTypes;
        return paramExceptionTypes;
    }

    private void parameterTypeIsAssignable(Class<? extends Throwable> paramType) {

        if (annotatedExceptions.length == 0) {
            return;
        }

        for (Class<? extends Throwable> excepctionType : annotatedExceptions) {
            if (paramType.isAssignableFrom(excepctionType)) {
                return;
            }
        }
        throw new IllegalStateException(
                String.format("Method parameter [%s] exception is NOT matching annotated Exception [%s].",
                        paramType, Arrays.toString(annotatedExceptions)));
    }

    private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {

        parameterTypeIsAssignable(exceptionType);

        Method oldMethod = mappedMethods.put(exceptionType, method);
        if (oldMethod != null && !oldMethod.equals(method)) {
            throw new IllegalStateException("Ambiguous @GrpcExceptionHandler method mapped for [" +
                    exceptionType + "]: {" + oldMethod + ", " + method + "}");
        }
    }


    public <E extends Throwable> Map.Entry<Object, Method> resolveMethodWithInstance(Class<E> exceptionType) {

        Method value = extractExtendedThrowable(exceptionType);
        if (value == null) {
            return new SimpleImmutableEntry<>(null, null);
        }

        Class<?> methodClass = value.getDeclaringClass();
        Object key = annotatedBeans.values()
                .stream()
                .filter(obj -> methodClass.isAssignableFrom(obj.getClass()))
                .findFirst()
                .orElse(null);
        return new SimpleImmutableEntry<>(key, value);
    }

    public <E extends Throwable> boolean isMethodMappedForException(Class<E> exception) {
        return extractExtendedThrowable(exception) != null;
    }

    private <E extends Throwable> Method extractExtendedThrowable(Class<E> exception) {
        return mappedMethods.keySet()
                .stream().filter(clazz -> clazz.isAssignableFrom(exception))
                .findAny()
                .map(mappedMethods::get)
                .orElse(null);
    }


}

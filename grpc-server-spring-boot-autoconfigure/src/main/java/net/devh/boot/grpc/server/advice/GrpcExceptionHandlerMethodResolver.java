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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Given an annotated {@link GrpcAdvice @GrpcAdvice} class and annotated methods with
 * {@link GrpcExceptionHandler @GrpcExceptionHandler}, {@link GrpcExceptionHandlerMethodResolver} resolves given
 * exception type and maps it to the corresponding method to be executed, when this exception is being raised.
 *
 * <p>
 * For an example how to make use of it, please have a look at {@link GrpcExceptionHandler @GrpcExceptionHandler}.
 * <p>
 * 
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcAdvice
 * @see GrpcExceptionHandler
 * @see GrpcAdviceExceptionHandler
 */
public class GrpcExceptionHandlerMethodResolver implements InitializingBean {

    private final Map<Class<? extends Throwable>, Method> mappedMethods = new HashMap<>(16);

    private final GrpcAdviceDiscoverer grpcAdviceDiscoverer;

    private Class<? extends Throwable>[] annotatedExceptions;

    /**
     * Creates a new GrpcExceptionHandlerMethodResolver.
     *
     * @param grpcAdviceDiscoverer The advice discoverer to use.
     */
    public GrpcExceptionHandlerMethodResolver(final GrpcAdviceDiscoverer grpcAdviceDiscoverer) {
        this.grpcAdviceDiscoverer = requireNonNull(grpcAdviceDiscoverer, "grpcAdviceDiscoverer");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        grpcAdviceDiscoverer.getAnnotatedMethods()
                .forEach(this::extractAndMapExceptionToMethod);
    }

    private void extractAndMapExceptionToMethod(Method method) {

        GrpcExceptionHandler annotation = method.getDeclaredAnnotation(GrpcExceptionHandler.class);
        Assert.notNull(annotation, "@GrpcExceptionHandler annotation not found.");
        annotatedExceptions = annotation.value();

        checkForPresentExceptionToMap(method);
        Set<Class<? extends Throwable>> exceptionsToMap = extractExceptions(method.getParameterTypes());
        exceptionsToMap.forEach(exceptionType -> addExceptionMapping(exceptionType, method));
    }

    private void checkForPresentExceptionToMap(Method method) {
        if (method.getParameterTypes().length == 0 && annotatedExceptions.length == 0) {
            throw new IllegalStateException(
                    String.format("@GrpcExceptionHandler annotated method [%s] has no mapped exception!",
                            method.getName()));
        }
    }

    private Set<Class<? extends Throwable>> extractExceptions(Class<?>[] methodParamTypes) {

        Set<Class<? extends Throwable>> exceptionsToBeMapped = new HashSet<>();
        for (Class<? extends Throwable> annoClass : annotatedExceptions) {
            if (methodParamTypes.length > 0)
                validateAppropriateParentException(annoClass, methodParamTypes);
            exceptionsToBeMapped.add(annoClass);
        }

        addMappingInCaseAnnotationIsEmpty(methodParamTypes, exceptionsToBeMapped);
        return exceptionsToBeMapped;
    }

    private void validateAppropriateParentException(Class<? extends Throwable> annoClass, Class<?>[] methodParamTypes) {

        boolean paramTypeIsNotSuperclass =
                Arrays.stream(methodParamTypes).noneMatch(param -> param.isAssignableFrom(annoClass));
        if (paramTypeIsNotSuperclass) {
            throw new IllegalStateException(
                    String.format(
                            "no listed parameter argument [%s] is equal or superclass "
                                    + "of annotated @GrpcExceptionHandler method declared exception [%s].",
                            Arrays.toString(methodParamTypes), annoClass));
        }
    }

    private void addMappingInCaseAnnotationIsEmpty(
            Class<?>[] methodParamTypes,
            Set<Class<? extends Throwable>> exceptionsToBeMapped) {

        @SuppressWarnings("unchecked")
        Function<Class<?>, Class<? extends Throwable>> convertSafely = clazz -> (Class<? extends Throwable>) clazz;

        Arrays.stream(methodParamTypes)
                .filter(param -> exceptionsToBeMapped.isEmpty())
                .filter(Throwable.class::isAssignableFrom)
                .map(convertSafely) // safe to call, since check for Throwable superclass
                .forEach(exceptionsToBeMapped::add);
    }

    private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {

        Method oldMethod = mappedMethods.put(exceptionType, method);
        if (oldMethod != null && !oldMethod.equals(method)) {
            throw new IllegalStateException("Ambiguous @GrpcExceptionHandler method mapped for [" +
                    exceptionType + "]: {" + oldMethod + ", " + method + "}");
        }
    }


    /**
     * When given exception type is subtype of already provided mapped exception, this returns a valid mapped method to
     * be later executed.
     * 
     * @param exceptionType exception to check
     * @param <E> type of exception
     * @return mapped method instance with its method
     */
    @NonNull
    public <E extends Throwable> Map.Entry<Object, Method> resolveMethodWithInstance(Class<E> exceptionType) {

        Method value = extractExtendedThrowable(exceptionType);
        if (value == null) {
            return new SimpleImmutableEntry<>(null, null);
        }

        Class<?> methodClass = value.getDeclaringClass();
        Object key = grpcAdviceDiscoverer.getAnnotatedBeans()
                .values()
                .stream()
                .filter(obj -> methodClass.isAssignableFrom(obj.getClass()))
                .findFirst()
                .orElse(null);
        return new SimpleImmutableEntry<>(key, value);
    }

    /**
     * Lookup if a method is mapped to given exception.
     * 
     * @param exception exception to check
     * @param <E> type of exception
     * @return true if mapped to valid method
     */
    public <E extends Throwable> boolean isMethodMappedForException(Class<E> exception) {
        return extractExtendedThrowable(exception) != null;
    }

    private <E extends Throwable> Method extractExtendedThrowable(Class<E> exception) {
        return mappedMethods.keySet()
                .stream()
                .filter(clazz -> clazz.isAssignableFrom(exception))
                .findAny()
                .map(mappedMethods::get)
                .orElse(null);
    }

}

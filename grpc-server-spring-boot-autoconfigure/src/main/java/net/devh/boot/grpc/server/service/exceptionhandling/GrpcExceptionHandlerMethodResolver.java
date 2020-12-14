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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * Given an annotated {@link GrpcServiceAdvice @GrpcServiceAdvice} class and annotated methods with
 * {@link GrpcExceptionHandler @GrpcExceptionHandler}, {@link GrpcExceptionHandlerMethodResolver} resolves given
 * exception type and maps it to the corresponding method to be executed, when this exception is being raised.<br>
 * For an example how to make use of it, please have a look {@link GrpcExceptionHandler @GrpcExceptionHandler}.<br>
 * <br>
 * 
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcServiceAdvice
 * @see GrpcExceptionHandler
 * @see GrpcServiceAdviceExceptionHandler
 */
@Slf4j
public class GrpcExceptionHandlerMethodResolver implements InitializingBean {

    private final Map<Class<? extends Throwable>, Method> mappedMethods = new HashMap<>(16);

    private final GrpcServiceAdviceDiscoverer grpcServiceAdviceDiscoverer;

    private Class<? extends Throwable>[] annotatedExceptions;

    public GrpcExceptionHandlerMethodResolver(final GrpcServiceAdviceDiscoverer grpcServiceAdviceDiscoverer) {
        this.grpcServiceAdviceDiscoverer = grpcServiceAdviceDiscoverer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        grpcServiceAdviceDiscoverer.getAnnotatedMethods()
                .forEach(this::extractAndMapExceptionToMethod);
    }

    private void extractAndMapExceptionToMethod(Method method) {

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

    private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {

        parameterTypeIsAssignable(exceptionType);

        Method oldMethod = mappedMethods.put(exceptionType, method);
        if (oldMethod != null && !oldMethod.equals(method)) {
            throw new IllegalStateException("Ambiguous @GrpcExceptionHandler method mapped for [" +
                    exceptionType + "]: {" + oldMethod + ", " + method + "}");
        }
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


    public <E extends Throwable> Map.Entry<Object, Method> resolveMethodWithInstance(Class<E> exceptionType) {

        Method value = extractExtendedThrowable(exceptionType);
        if (value == null) {
            return new SimpleImmutableEntry<>(null, null);
        }

        Class<?> methodClass = value.getDeclaringClass();
        Object key = grpcServiceAdviceDiscoverer.getAnnotatedBeans()
                .values()
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

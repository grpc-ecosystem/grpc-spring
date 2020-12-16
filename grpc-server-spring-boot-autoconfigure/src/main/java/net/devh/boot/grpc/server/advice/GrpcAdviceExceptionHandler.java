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

package net.devh.boot.grpc.server.advice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.lang.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * As part of {@link GrpcAdvice @GrpcAdvice}, when a thrown exception is caught during gRPC calls (via global
 * interceptor {@link GrpcAdviceExceptionInterceptor}, then this thrown exception is being handled. By
 * {@link GrpcExceptionHandlerMethodResolver} is a mapping between exception and the in case to be executed method
 * provided. <br>
 * Returned object is declared in {@link GrpcAdvice @GrpcAdvice} classes with annotated methods
 * {@link GrpcExceptionHandler @GrpcExceptionHandler}.
 * <p>
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcExceptionHandlerMethodResolver
 * @see GrpcAdviceExceptionInterceptor
 */
@Slf4j
@Aspect
public class GrpcAdviceExceptionHandler {

    private final GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver;

    public GrpcAdviceExceptionHandler(
            final GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver) {
        this.grpcExceptionHandlerMethodResolver = grpcExceptionHandlerMethodResolver;
    }

    /**
     * Given an exception, a lookup is performed to retrieve mapped method. <br>
     * In case of successful returned method, and matching exception parameter type for given exception, the exception
     * is handed over to retrieved method. Retrieved method is then being invoked.
     * 
     * @param exception exception to search for
     * @param <E> type of exception
     * @return result of invoked mapped method to given exception
     * @throws Throwable rethrows exception if no mapping existent or exceptions raised by implementation
     */
    @Nullable
    public <E extends Throwable> Object handleThrownException(E exception) throws Throwable {

        final Class<? extends Throwable> exceptionClass = exception.getClass();
        boolean exceptionIsMapped =
                grpcExceptionHandlerMethodResolver.isMethodMappedForException(exceptionClass);
        if (!exceptionIsMapped) {
            throw exception;
        }

        Entry<Object, Method> methodWithInstance =
                grpcExceptionHandlerMethodResolver.resolveMethodWithInstance(exceptionClass);
        Method mappedMethod = methodWithInstance.getValue();
        Object instanceOfMappedMethod = methodWithInstance.getKey();
        Object[] instancedParams = determineInstancedParameters(mappedMethod, exception);

        return invokeMappedMethodSafely(mappedMethod, instanceOfMappedMethod, instancedParams);
    }

    private <E extends Throwable> Object[] determineInstancedParameters(Method mappedMethod, E exception) {

        Parameter[] parameters = mappedMethod.getParameters();
        Object[] instancedParams = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterClass = convertToClass(parameters[i]);
            if (parameterClass.isAssignableFrom(exception.getClass())) {
                instancedParams[i] = exception;
                break;
            }
        }
        return instancedParams;
    }

    private Class<?> convertToClass(Parameter parameter) {
        Type paramType = parameter.getParameterizedType();
        if (paramType instanceof Class) {
            return (Class<?>) paramType;
        }
        throw new IllegalStateException("Parameter type of method has to be from Class, it was: " + paramType);
    }

    private Object invokeMappedMethodSafely(
            Method mappedMethod,
            Object instanceOfMappedMethod,
            Object[] instancedParams) throws Throwable {
        Object statusThrowable = null;
        try {
            statusThrowable = mappedMethod.invoke(instanceOfMappedMethod, instancedParams);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw e.getCause(); // throw the exception thrown by implementation
        }
        return statusThrowable;
    }

}

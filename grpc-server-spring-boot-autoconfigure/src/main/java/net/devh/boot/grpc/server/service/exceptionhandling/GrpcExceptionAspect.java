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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handling for thrown {@link RuntimeException} inside annotated CLasses with
 * {@link net.devh.boot.grpc.server.service.GrpcService @GrpcService}, which implement {@link io.grpc.BindableService}.
 * <p>
 * After calling the mapped Methods to the corresponding Exception, the returned {@link Throwable} is beeing send to the
 * {@link io.grpc.stub.StreamObserver#onError(Throwable)}.
 * 
 * TODO...
 *
 * @author Andjelko (andjelko.perisic@gmail.com)
 */
@Slf4j
@Aspect
@Component
@ConditionalOnBean(annotation = GrpcServiceAdvice.class)
public class GrpcExceptionAspect {

    private final GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver;

    private Throwable exception;
    private Method mappedMethod;
    private Object instanceOfMappedMethod;

    public GrpcExceptionAspect(final GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver) {
        this.grpcExceptionHandlerMethodResolver = grpcExceptionHandlerMethodResolver;
    }


    @Pointcut("within(@net.devh.boot.grpc.server.service.GrpcService *)")
    void grpcServiceAnnotatedPointcut() {}

    @Pointcut("within(io.grpc.BindableService+)")
    void implementedBindableServicePointcut() {}

    @AfterThrowing(
            pointcut = "grpcServiceAnnotatedPointcut() && implementedBindableServicePointcut()",
            throwing = "exception")
    public <E extends Throwable> void handleExceptionInsideGrpcService(JoinPoint joinPoint, E exception) {
        log.error("Exception caught during gRPC service execution: ", exception);
        this.exception = exception;

        if (!grpcExceptionHandlerMethodResolver.isMethodMappedForException(exception.getClass())) {
            return;
        }
        extractNecessaryInformation();
        Throwable throwable = invokeMappedMethodSafely();
        closeStreamObserverOnError(joinPoint.getArgs(), throwable);
    }

    private void extractNecessaryInformation() {

        final Class<? extends Throwable> exceptionClass = exception.getClass();

        Entry<Object, Method> methodWithInstance =
                grpcExceptionHandlerMethodResolver.resolveMethodWithInstance(exceptionClass);
        mappedMethod =
                Optional.of(methodWithInstance)
                        .map(Entry::getValue)
                        .orElseThrow(() -> new IllegalStateException(
                                "No mapped method found for Exception " + exceptionClass));
        instanceOfMappedMethod =
                Optional.of(methodWithInstance)
                        .map(Entry::getKey)
                        .orElseThrow(() -> new IllegalStateException(
                                " No mapped instance found for Exception " + exceptionClass));
    }

    private Throwable invokeMappedMethodSafely() {
        try {
            Object[] instancedParams = determineInstancedParameters(mappedMethod);
            Object statusThrowable = mappedMethod.invoke(instanceOfMappedMethod, instancedParams);
            return castToThrowable(statusThrowable);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new MethodExecutionException("Error during mapped exception method execution: ", e.getCause());
        }
    }

    private Object[] determineInstancedParameters(Method mappedMethod) {

        Parameter[] parameters = mappedMethod.getParameters();
        Object[] instancedParams = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterClass = convertToClass(parameters[i]);
            if (parameterClass.equals(exception.getClass())) {
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
        throw new IllegalStateException("Parametertype of method has to be from Class, it was: " + paramType);
    }

    private Throwable castToThrowable(Object statusThrowable) {
        return Optional.of(statusThrowable)
                .filter(thrbl -> thrbl instanceof Throwable)
                .map(thrbl -> (Throwable) thrbl)
                .orElseThrow(() -> new IllegalStateException(
                        "Return type has to beo f type java.lang.Throable: " + statusThrowable));
    }

    private void closeStreamObserverOnError(Object[] joinPointParams, Throwable throwable) {
        for (Object param : joinPointParams) {
            if (param instanceof StreamObserver) {
                StreamObserver<?> streamObserver = (StreamObserver<?>) param;
                streamObserver.onError(throwable);
            }
        }
    }

}

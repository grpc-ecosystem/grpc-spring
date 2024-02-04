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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.springframework.lang.Nullable;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.error.GrpcExceptionInterceptor;
import net.devh.boot.grpc.server.error.GrpcExceptionResponseHandler;

/**
 * As part of {@link GrpcAdvice @GrpcAdvice}, when a thrown exception is caught during gRPC calls (via global
 * interceptor {@link GrpcExceptionInterceptor}), then this thrown exception is being handled. The
 * {@link GrpcExceptionHandlerMethodResolver} provides a mapping for exceptions and their respective handler methods.
 *
 * <p>
 * The response is derived from methods annotated with {@link GrpcExceptionHandler} inside {@link GrpcAdvice} beans.
 * </p>
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcAdvice
 * @see GrpcExceptionHandler
 * @see GrpcExceptionHandlerMethodResolver
 * @see GrpcExceptionInterceptor
 */
@Slf4j
public class GrpcAdviceExceptionHandler implements GrpcExceptionResponseHandler {

    private final GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver;

    /**
     * Creates a new {@link GrpcAdvice} powered {@link GrpcExceptionHandler}.
     *
     * @param grpcExceptionHandlerMethodResolver The method resolver to use.
     */
    public GrpcAdviceExceptionHandler(
            final GrpcExceptionHandlerMethodResolver grpcExceptionHandlerMethodResolver) {
        this.grpcExceptionHandlerMethodResolver =
                requireNonNull(grpcExceptionHandlerMethodResolver, "grpcExceptionHandlerMethodResolver");
    }

    @Override
    public void handleError(final ServerCall<?, ?> serverCall, final Throwable error) {
        try {
            final Object mappedReturnType = handleThrownException(error);
            final Status status = resolveStatus(mappedReturnType);
            final Metadata metadata = resolveMetadata(mappedReturnType);

            serverCall.close(status, metadata);
        } catch (final Throwable errorWhileResolving) {
            if (errorWhileResolving != error) {
                errorWhileResolving.addSuppressed(error);
            }
            handleThrownExceptionByImplementation(serverCall, errorWhileResolving);
        }
    }

    protected Status resolveStatus(final Object mappedReturnType) {
        if (mappedReturnType instanceof Status) {
            return (Status) mappedReturnType;
        } else if (mappedReturnType instanceof Throwable) {
            return Status.fromThrowable((Throwable) mappedReturnType);
        }
        throw new IllegalStateException(String.format(
                "Error for mapped return type [%s] inside @GrpcAdvice, it has to be of type: "
                        + "[Status, StatusException, StatusRuntimeException, Throwable] ",
                mappedReturnType));
    }

    protected Metadata resolveMetadata(final Object mappedReturnType) {
        Metadata result = null;
        if (mappedReturnType instanceof StatusException) {
            final StatusException statusException = (StatusException) mappedReturnType;
            result = statusException.getTrailers();
        } else if (mappedReturnType instanceof StatusRuntimeException) {
            final StatusRuntimeException statusException = (StatusRuntimeException) mappedReturnType;
            result = statusException.getTrailers();
        }
        return (result == null) ? new Metadata() : result;
    }

    protected void handleThrownExceptionByImplementation(final ServerCall<?, ?> serverCall, final Throwable throwable) {
        log.error("Exception thrown during invocation of annotated @GrpcExceptionHandler method: ", throwable);
        serverCall.close(Status.INTERNAL.withCause(throwable)
                .withDescription("There was a server error trying to handle an exception"), new Metadata());
    }

    /**
     * Given an exception, a lookup is performed to retrieve mapped method. <br>
     * In case of successful returned method, and matching exception parameter type for given exception, the exception
     * is handed over to retrieved method. Retrieved method is then being invoked.
     *
     * @param exception exception to search for
     * @return result of invoked mapped method to given exception
     * @throws Throwable rethrows exception if no mapping existent or exceptions raised by implementation
     */
    @Nullable
    protected Object handleThrownException(final Throwable exception) throws Throwable {
        log.debug("Exception caught during gRPC execution: ", exception);

        final Class<? extends Throwable> exceptionClass = exception.getClass();
        final boolean exceptionIsMapped =
                this.grpcExceptionHandlerMethodResolver.isMethodMappedForException(exceptionClass);
        if (!exceptionIsMapped) {
            throw exception;
        }

        final Entry<Object, Method> methodWithInstance =
                this.grpcExceptionHandlerMethodResolver.resolveMethodWithInstance(exceptionClass);
        final Method mappedMethod = methodWithInstance.getValue();
        final Object instanceOfMappedMethod = methodWithInstance.getKey();
        final Object[] instancedParams = determineInstancedParameters(mappedMethod, exception);

        return invokeMappedMethodSafely(mappedMethod, instanceOfMappedMethod, instancedParams);
    }

    private Object[] determineInstancedParameters(final Method mappedMethod, final Throwable exception) {

        final Parameter[] parameters = mappedMethod.getParameters();
        final Object[] instancedParams = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            final Class<?> parameterClass = convertToClass(parameters[i]);
            if (parameterClass.isAssignableFrom(exception.getClass())) {
                instancedParams[i] = exception;
                break;
            }
        }
        return instancedParams;
    }

    private Class<?> convertToClass(final Parameter parameter) {
        final Type paramType = parameter.getParameterizedType();
        if (paramType instanceof Class) {
            return (Class<?>) paramType;
        }
        throw new IllegalStateException("Parameter type of method has to be from Class, it was: " + paramType);
    }

    private Object invokeMappedMethodSafely(
            final Method mappedMethod,
            final Object instanceOfMappedMethod,
            final Object[] instancedParams) throws Throwable {
        try {
            return mappedMethod.invoke(instanceOfMappedMethod, instancedParams);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw e.getCause(); // throw the exception thrown by implementation
        }
    }

}

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

package net.devh.boot.grpc.server.validation;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.protobuf.MessageLiteOrBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Resolving all classes implementing {@link GrpcConstraintValidator} and marked with annotation
 * {@link GrpcConstraint @GrpcConstraint}. Resolved classes are validation classes for gRPC requests to be validated.
 * <p>
 * The Validation is done via {@link RequestValidationInterceptor}. There can be more than one validation class for the
 * same request type, all of them are being resolved and used for validation.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcConstraintValidator
 * @see RequestValidationInterceptor
 */
@Slf4j
class GrpcValidationResolver implements InitializingBean, ApplicationContextAware {

    private Map<String, GrpcConstraintValidator<MessageLiteOrBuilder>> validatorMap;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        validatorMap = applicationContext.getBeansWithAnnotation(GrpcConstraint.class)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, this::convertSafely));
        log.debug("Found {} gRPC validators", validatorMap.size());
    }


    private GrpcConstraintValidator<MessageLiteOrBuilder> convertSafely(Map.Entry<String, Object> entry) {

        Object annotatedValidator = entry.getValue();
        if (annotatedValidator instanceof GrpcConstraintValidator) {
            @SuppressWarnings("unchecked")
            GrpcConstraintValidator<MessageLiteOrBuilder> safeConstraintInstance =
                    (GrpcConstraintValidator<MessageLiteOrBuilder>) annotatedValidator;
            return safeConstraintInstance;
        }

        throw new IllegalStateException(
                String.format("@GrpcConstraint annotated class [%s] has to implement GrpcConstraintValidator.class",
                        annotatedValidator.getClass()));
    }

    /**
     * Retrieve all {@link GrpcConstraintValidator} which are the same class or at least a superclass of given input
     * parameter.
     *
     * @param request gRPC request
     * @param <E> type of the gRPC request message
     * @return validators to be used in conjunction with the request
     */
    <E> List<GrpcConstraintValidator<MessageLiteOrBuilder>> findValidators(E request) {
        return validatorMap.values()
                .stream()
                .filter(cs -> checkForGenericTypeArgument(cs, request))
                .collect(Collectors.toList());
    }

    private <E> boolean checkForGenericTypeArgument(
            GrpcConstraintValidator<MessageLiteOrBuilder> grpcConstraintValidator, E request) {

        List<Type> genericTypes = Arrays.asList(grpcConstraintValidator.getClass().getGenericInterfaces());

        return genericTypes.stream()
                .map(t -> (ParameterizedType) t)
                .flatMap(pt -> Arrays.stream(pt.getActualTypeArguments()))
                .map(t -> (Class<?>) t)
                .anyMatch(c -> c.isAssignableFrom(request.getClass()));
    }

}

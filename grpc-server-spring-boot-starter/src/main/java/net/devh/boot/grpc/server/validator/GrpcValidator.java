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

package net.devh.boot.grpc.server.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Annotation to mark methods that require gRPC validation. This annotation is typically used for methods in a service
 * class where a gRPC request needs to be validated using a specific validator class and method.
 *
 * @author Pritesh (priteshdpawar53@gmail.com)
 * @since 02/10/25
 *
 * <p>
 * Usage Example:
 * </p>
 * 
 * <pre>
 * {@code @GrpcValidator(requestClass = MyRequest.class, validatorClass = MyRequestValidator.class, validatorMethod = "validateRequest")
 * public void myGrpcMethod(MyRequest request) {
 *     // Your gRPC method implementation
 * }
 * }
 * </pre>
 *
 * <p>
 * In this example, the annotation tells the system to use the {@code MyRequestValidator} class and call its
 * {@code validateRequest} method to validate the incoming {@code MyRequest}.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GrpcValidator {

    /**
     * The request class that will be validated.
     *
     * <p>
     * This represents the type of the gRPC request that needs validation. The validator will perform checks based on
     * this class.
     * </p>
     *
     * @return the class type of the gRPC request
     */
    Class<?> requestClass();

    /**
     * The validator class that contains the validation logic.
     *
     * <p>
     * This class is responsible for validating the request class. It should provide the validation method specified by
     * {@link #validatorMethod()}.
     * </p>
     *
     * @return the class type of the validator
     */
    Class<?> validatorClass();

    /**
     * The name of the method in the validator class that performs the validation.
     *
     * <p>
     * This method is invoked at runtime to perform the validation logic on the request class. It should take the
     * request class type as an argument and return a validation result, typically a boolean or a validation exception.
     * </p>
     *
     * @return the name of the validation method
     */
    String validatorMethod();
}

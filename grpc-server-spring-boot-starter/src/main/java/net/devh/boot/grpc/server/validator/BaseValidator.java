/*
 * Copyright (c) 2016-2025 The gRPC-Spring Authors
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

import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidationException;
import io.envoyproxy.pgv.ValidatorIndex;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * The {@code BaseValidator} interface provides a default method for validating gRPC request objects
 * using the Envoy Proxy's Protoc-Gen-Validate library.
 *
 * @author Pritesh (priteshdpawar53@gmail.com)
 * @since 02/10/25
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * public class MyRequestValidator implements BaseValidator {
 *     public void validateMyRequest(MyRequest request) {
 *         // Business validation
 *     }
 * }
 * }
 * </pre>
 * </p>
 */
public interface BaseValidator {

    /**
     * Validates the provided request object using the Envoy Proxy Protoc-Gen-Validate library.
     * <p>
     * This method uses the {@link ValidatorIndex} (specifically the {@link ReflectiveValidatorIndex}) to look up
     * the appropriate validator for the request's class and performs validation. If the request is invalid, a
     * {@link StatusRuntimeException} with a {@link Status} status is thrown, including details
     * of the validation failure.
     * </p>
     *
     * @param request the request object to validate
     * @param <T>     the type of the request object
     * @throws StatusRuntimeException if the validation fails, this exception is thrown with a description of the failure
     *         and a cause explaining the validation issue.
     */
    default <T> void validate(T request) {
        ValidatorIndex validatorIndex = new ReflectiveValidatorIndex();
        try {
            validatorIndex.validatorFor(request.getClass()).assertValid(request);
        } catch (ValidationException e) {
            throw new StatusRuntimeException(Status.FAILED_PRECONDITION.withCause(e)
                    .withDescription(e.getField() + " : " + e.getReason() + ". Received value: " + e.getMessage()));
        }
    }
}

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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with {@link GrpcExceptionHandler @GrpcExceptionHandler} are being mapped to a corresponding
 * Exception, by declaring either in {@link GrpcExceptionHandler#value() @GrpcExceptionHandler(value = ...)} as value or
 * as annotated methods parameter (both is working too).
 * <p>
 * Return type of annotated methods has to be of type {@link io.grpc.Status}, {@link io.grpc.StatusException},
 * {@link io.grpc.StatusRuntimeException} or {@link Throwable}.
 * <p>
 *
 * An example without {@link io.grpc.Metadata}:
 * 
 * <pre>
 * {@code @GrpcExceptionHandler
 * public Status handleIllegalArgumentException(IllegalArgumentException e) {
 *     return Status.INVALID_ARGUMENT
 *             .withDescription(e.getMessage())
 *             .withCause(e);
 * }
 * }
 * </pre>
 * 
 * <b>With</b> {@link io.grpc.Metadata}:
 * 
 * <pre>
 * {@code @GrpcExceptionHandler
 *    public StatusRuntimeException handleIllegalArgumentException(IllegalArgumentException e){
 *      Status status = Status.INVALID_ARGUMENT
 *                            .withDescription(e.getMessage())
 *                            .withCause(e);
 *      Metadata myMetadata = new Metadata();
 *      myMetadata = ...
 *      return status.asRuntimeException(myMetadata);
 *    }
 *  }
 * </pre>
 * 
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcAdvice
 * @see GrpcExceptionHandlerMethodResolver
 * @see GrpcAdviceExceptionHandler
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcExceptionHandler {

    /**
     * Exceptions handled by the annotated method.
     * <p>
     * If empty, will default to any exceptions listed in the method argument list.
     * <p>
     * <b>Note:</b> When exception types are set within value, they are prioritized in mapping the exceptions over
     * listed method arguments. And in case method arguments are provided, they <b>must</b> match the types declared
     * with this value.
     */
    Class<? extends Throwable>[] value() default {};
}

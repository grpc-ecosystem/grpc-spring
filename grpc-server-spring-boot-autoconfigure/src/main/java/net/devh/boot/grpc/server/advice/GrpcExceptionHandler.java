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

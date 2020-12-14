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
 * Return type of annotated methods has to be of type {@link Throwable} or {@link io.grpc.Status}, the latter is wrapped
 * up later as {@link io.grpc.StatusRuntimeException}. For more detailed information
 * {@link GrpcExceptionHandlerMethodResolver}. <br>
 * <p>
 *
 * As an example, this is the preferred way of handling exception,
 * 
 * <pre>
 * {@code @GrpcExceptionHandler
 *    public Status handleIllegalArgumentException(IllegalArgumentException e){
 *      return Status.INVALID_ARGUMENT
 *                   .withDescription(e.getMessage())
 *                   .withCause(e);
 *    }
 *  }
 * </pre>
 * 
 * but the following is also possible, especially if {@link io.grpc.Metadata} has to be returned.
 * 
 * <pre>
 * {@code @GrpcExceptionHandler
 *    public StatusRuntimeException handleIllegalArgumentException(IllegalArgumentException e){
 *      Status status = Status.INVALID_ARGUMENT
 *                            .withDescription(e.getMessage())
 *                            .withCause(e);
 *      return status.asRuntimeException();
 *    }
 *  }
 * </pre>
 * 
 * Further when an {@link Exception} is raised by the application during runtime,
 * {@link GrpcServiceAdviceExceptionHandler} interrupts after thrown exception and executes above mentioned annotated
 * method which was mapped by {@link GrpcExceptionHandler @GrpcExceptionHandler} inside a class annotated with
 * {@link GrpcServiceAdvice @GrpcServiceAdvice}.<br>
 * <p>
 * 
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcServiceAdvice
 * @see GrpcExceptionHandlerMethodResolver
 * @see GrpcServiceAdviceExceptionHandler
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcExceptionHandler {

    /**
     * Exceptions handled by the annotated method.
     * 
     * If empty, will default to any exceptions listed in the method argument list.
     */
    Class<? extends Throwable>[] value() default {};
}

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

import com.google.protobuf.MessageLiteOrBuilder;

import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Implement this interface to perform a request validation for incoming gRPC messages. Subsequently requests received
 * in {@link GrpcService @GrpcService} are validated.<br>
 * <b>Hint: </b> Also annotate class with {@link GrpcConstraint @GrpcConstraint} to be picked up.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcValidationResolver
 * @see GrpcConstraint
 */
public interface GrpcConstraintValidator<E extends MessageLiteOrBuilder> {

    /**
     * Method invoked to check wheter validation succeds. In case an exeception occurs a
     * {@link io.grpc.Status.Code#INTERNAL} is sent back to the client with the thrown exception message.
     *
     * @param request gRPC request
     * @return {@code true} if validation successfull, {@code false otherwise}
     */
    boolean isValid(E request);

}

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

import java.util.List;
import java.util.stream.Collectors;

import com.google.protobuf.MessageLiteOrBuilder;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

/**
 * Responsible for a proper server-side validation of incoming gRPC requests. There is no restriction in how many
 * validations of a request type can exist. Every one of them is being applied and validated.<br>
 * When a validation fails, a {@link Status.Code#INVALID_ARGUMENT} is returned with
 * {@link ServerCall#close(Status, Metadata)}. In case an exception is raised inside
 * {@link GrpcConstraintValidator#isValid(MessageLiteOrBuilder)} the {@code Status} is {@link Status.Code#INTERNAL} with
 * a message from raised exception.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 * @see GrpcValidationResolver
 */
@Slf4j
class RequestValidationListener<ReqT, RespT> extends SimpleForwardingServerCallListener<ReqT> {

    private final ServerCall<ReqT, RespT> serverCall;
    private final Metadata headers;
    private final GrpcValidationResolver grpcValidationResolver;

    protected RequestValidationListener(
            Listener<ReqT> delegate,
            ServerCall<ReqT, RespT> serverCall,
            Metadata headers,
            GrpcValidationResolver grpcValidationResolver) {
        super(delegate);
        this.serverCall = serverCall;
        this.headers = headers;
        this.grpcValidationResolver = grpcValidationResolver;
    }

    @Override
    public void onMessage(ReqT message) {

        List<GrpcConstraintValidator<MessageLiteOrBuilder>> validatorList =
                grpcValidationResolver.findValidators(message);
        MessageLiteOrBuilder convertedMessage = (MessageLiteOrBuilder) message;
        boolean requestIsNotValid = validatorList.stream().anyMatch(v -> isNotValid(v, convertedMessage));

        if (requestIsNotValid) {
            handleInvalidRequest(validatorList);
        } else {
            super.onMessage(message);
        }
    }

    private boolean isNotValid(
            GrpcConstraintValidator<MessageLiteOrBuilder> validator,
            MessageLiteOrBuilder convertedMessage) {

        try {
            return !validator.isValid(convertedMessage);
        } catch (Throwable t) {
            log.error("Error during validation: " + t);
            Status status = Status.INTERNAL.withDescription(t.getMessage());
            serverCall.close(status, headers);
            return true;
        }
    }

    private void handleInvalidRequest(List<GrpcConstraintValidator<MessageLiteOrBuilder>> validatorList) {

        String validators = validatorList.stream()
                .map(v -> v.getClass().getSimpleName())
                .collect(Collectors.joining(", "));

        String errorMsg = String.format("Validation error at least in one of [%s]", validators);
        Status status = Status.INVALID_ARGUMENT.withDescription(errorMsg);
        serverCall.close(status, headers);
    }

}

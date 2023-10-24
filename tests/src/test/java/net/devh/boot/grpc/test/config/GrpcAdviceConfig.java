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

package net.devh.boot.grpc.test.config;

import java.security.AccessControlException;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.security.authentication.AccountExpiredException;

import com.google.protobuf.Empty;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.test.advice.GrpcMetaDataUtils;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

@Configuration
public class GrpcAdviceConfig {

    @GrpcService
    public static class TestGrpcAdviceService extends TestServiceGrpc.TestServiceImplBase {

        private Supplier<? extends RuntimeException> throwableToSimulate;
        private LocationToThrow throwLocation;

        @Override
        public void normal(final Empty request, final StreamObserver<SomeType> responseObserver) {

            Assertions.assertThat(this.throwableToSimulate).isNotNull();
            switch (this.throwLocation) {
                case METHOD:
                    throw this.throwableToSimulate.get();
                case RESPONSE_OBSERVER:
                    responseObserver.onError(this.throwableToSimulate.get());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported LocationToThrow: " + this.throwLocation);
            }
        }

        @Override
        public StreamObserver<SomeType> echo(final StreamObserver<SomeType> responseObserver) {
            Assertions.assertThat(this.throwableToSimulate).isNotNull();
            switch (this.throwLocation) {
                case METHOD:
                    throw this.throwableToSimulate.get();
                case RESPONSE_OBSERVER:
                    responseObserver.onError(this.throwableToSimulate.get());
                    return responseObserver;
                case REQUEST_OBSERVER_ON_NEXT:
                    return new StreamObserver<SomeType>() {

                        @Override
                        public void onNext(final SomeType value) {
                            throw TestGrpcAdviceService.this.throwableToSimulate.get();
                        }

                        @Override
                        public void onError(final Throwable t) {
                            responseObserver.onError(t);
                        }

                        @Override
                        public void onCompleted() {
                            responseObserver.onCompleted();
                        }

                    };
                case REQUEST_OBSERVER_ON_COMPLETION:
                    return new StreamObserver<SomeType>() {

                        @Override
                        public void onNext(final SomeType value) {
                            responseObserver.onNext(value);
                        }

                        @Override
                        public void onError(final Throwable t) {
                            responseObserver.onError(t);
                        }

                        @Override
                        public void onCompleted() {
                            throw TestGrpcAdviceService.this.throwableToSimulate.get();
                        }

                    };
                default:
                    throw new UnsupportedOperationException("Unsupported LocationToThrow: " + this.throwLocation);
            }
        }

        public void setExceptionToSimulate(final Supplier<? extends RuntimeException> exception) {
            this.throwableToSimulate = exception;
        }

        public void setThrowLocation(final LocationToThrow throwLocation) {
            this.throwLocation = throwLocation;
        }


    }

    @GrpcAdvice
    @Bean
    public TestAdviceWithOutMetadata grpcAdviceWithBean() {
        return new TestAdviceWithOutMetadata();
    }

    public enum LocationToThrow {

        METHOD(false),

        RESPONSE_OBSERVER(false),

        REQUEST_OBSERVER_ON_NEXT(true),

        REQUEST_OBSERVER_ON_COMPLETION(true);

        private final boolean streaming;

        LocationToThrow(final boolean streaming) {
            this.streaming = streaming;
        }

        public boolean isForStreamingOnly() {
            return this.streaming;
        }


    }

    public static class TestAdviceWithOutMetadata {

        @GrpcExceptionHandler
        public Status handleIllegalArgumentException(final IllegalArgumentException e) {
            return Status.INVALID_ARGUMENT.withCause(e).withDescription(e.getMessage());
        }

        @GrpcExceptionHandler({ConversionFailedException.class, AccessControlException.class})
        public Throwable handleConversionFailedExceptionAndAccessControlException(
                final ConversionFailedException e1,
                final AccessControlException e2) {
            return (e1 != null) ? e1 : ((e2 != null) ? e2 : new RuntimeException("Should not happen."));
        }

        public Status methodNotToBePickup(final AccountExpiredException e) {
            Assertions.fail("Not supposed to be picked up.");
            return Status.FAILED_PRECONDITION;
        }
    }

    @GrpcAdvice
    public static class TestAdviceWithMetadata {

        @GrpcExceptionHandler(FirstLevelException.class)
        public StatusException handleFirstLevelException(final MyRootRuntimeException e) {

            final Status status = Status.NOT_FOUND.withCause(e).withDescription(e.getMessage());
            final Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();
            return status.asException(metadata);
        }

        @GrpcExceptionHandler(ClassCastException.class)
        public StatusRuntimeException handleClassCastException() {

            final Status status = Status.FAILED_PRECONDITION.withDescription("Casting with classes failed.");
            final Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();
            return status.asRuntimeException(metadata);
        }

        @GrpcExceptionHandler
        public StatusRuntimeException handleStatusMappingException(final StatusMappingException e) {

            throw new NullPointerException("Simulate developer error");
        }

    }


    @GrpcAdvice
    public static class TestAdviceForInheritedExceptions {


        @GrpcExceptionHandler(SecondLevelException.class)
        public Status handleSecondLevelException(final SecondLevelException e) {

            return Status.ABORTED.withCause(e).withDescription(e.getMessage());
        }

        @GrpcExceptionHandler
        public Status handleMyRootRuntimeException(final MyRootRuntimeException e) {

            return Status.DEADLINE_EXCEEDED.withCause(e).withDescription(e.getMessage());
        }

    }

    public static class MyRootRuntimeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public MyRootRuntimeException(final String msg) {
            super(msg);
        }
    }

    public static class FirstLevelException extends MyRootRuntimeException {

        private static final long serialVersionUID = 1L;

        public FirstLevelException(final String msg) {
            super(msg);
        }
    }

    public static class SecondLevelException extends FirstLevelException {

        private static final long serialVersionUID = 1L;

        public SecondLevelException(final String msg) {
            super(msg);
        }
    }

    public static class StatusMappingException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public StatusMappingException(final String msg) {
            super(msg);
        }
    }


}

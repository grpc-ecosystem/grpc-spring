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

package net.devh.boot.grpc.test.config;

import java.security.AccessControlException;

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
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithMetadata.MyRootRuntimeException;
import net.devh.boot.grpc.test.config.GrpcAdviceConfig.TestAdviceWithMetadata.SecondLevelException;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

@Configuration
public class GrpcAdviceConfig {

    @GrpcService
    public static class TestGrpcAdviceService extends TestServiceGrpc.TestServiceImplBase {

        private RuntimeException throwableToSimulate;

        @Override
        public void normal(final Empty request, final StreamObserver<SomeType> responseObserver) {

            Assertions.assertThat(throwableToSimulate).isNotNull();
            throw throwableToSimulate;
        }

        public <E extends RuntimeException> void setExceptionToSimulate(E exception) {
            throwableToSimulate = exception;
        }
    }

    @GrpcAdvice
    @Bean
    public TestAdviceWithOutMetadata grpcAdviceWithBean() {
        return new TestAdviceWithOutMetadata();
    }

    public static class TestAdviceWithOutMetadata {

        @GrpcExceptionHandler
        public Status handleIllegalArgumentException(IllegalArgumentException e) {
            return Status.INVALID_ARGUMENT.withCause(e).withDescription(e.getMessage());
        }

        @GrpcExceptionHandler({ConversionFailedException.class, AccessControlException.class})
        public Throwable handleConversionFailedExceptionAndAccessControlException(
                ConversionFailedException e1,
                AccessControlException e2) {
            return (e1 != null) ? e1 : ((e2 != null) ? e2 : new RuntimeException("Should not happen."));
        }

        public Status methodNotToBePickup(AccountExpiredException e) {
            Assertions.fail("Not supposed to be picked up.");
            return Status.FAILED_PRECONDITION;
        }
    }

    @GrpcAdvice
    public static class TestAdviceWithMetadata {

        @GrpcExceptionHandler(FirstLevelException.class)
        public StatusException handleFirstLevelException(MyRootRuntimeException e) {

            Status status = Status.NOT_FOUND.withCause(e).withDescription(e.getMessage());
            Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();
            return status.asException(metadata);
        }

        @GrpcExceptionHandler(ClassCastException.class)
        public StatusRuntimeException handleClassCastException() {

            Status status = Status.FAILED_PRECONDITION.withDescription("Casting with classes failed.");
            Metadata metadata = GrpcMetaDataUtils.createExpectedAsciiHeader();
            return status.asRuntimeException(metadata);
        }

        @GrpcExceptionHandler
        public StatusRuntimeException handleStatusMappingException(StatusMappingException e) {

            throw new NullPointerException("Simulate developer error");
        }


        public static class MyRootRuntimeException extends RuntimeException {

            public MyRootRuntimeException(String msg) {
                super(msg);
            }
        }

        public static class FirstLevelException extends MyRootRuntimeException {

            public FirstLevelException(String msg) {
                super(msg);
            }
        }

        public static class SecondLevelException extends FirstLevelException {

            public SecondLevelException(String msg) {
                super(msg);
            }
        }

        public static class StatusMappingException extends RuntimeException {

            public StatusMappingException(String msg) {
                super(msg);
            }
        }

    }


    @GrpcAdvice
    public static class TestAdviceForInheritedExceptions {


        @GrpcExceptionHandler(SecondLevelException.class)
        public Status handleSecondLevelException(SecondLevelException e) {

            return Status.ABORTED.withCause(e).withDescription(e.getMessage());
        }

        @GrpcExceptionHandler
        public Status handleMyRootRuntimeException(MyRootRuntimeException e) {

            return Status.DEADLINE_EXCEEDED.withCause(e).withDescription(e.getMessage());
        }

    }


}

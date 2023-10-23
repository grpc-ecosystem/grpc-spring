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

package net.devh.boot.grpc.test.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Empty;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.security.interceptors.AuthenticatingServerInterceptor;
import net.devh.boot.grpc.server.service.GrpcService;
import net.devh.boot.grpc.test.proto.SomeType;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;
import net.devh.boot.grpc.test.proto.TestServiceGrpc.TestServiceImplBase;

@Slf4j
@GrpcService
public class TestServiceImpl extends TestServiceImplBase {

    public static final int METHOD_COUNT = TestServiceGrpc.getServiceDescriptor().getMethods().size();

    public TestServiceImpl() {
        log.info("Created TestServiceImpl");
    }

    @Override
    public void normal(final Empty request, final StreamObserver<SomeType> responseObserver) {
        log.debug("normal");
        final SomeType version = SomeType.newBuilder().setVersion("1.2.3").build();
        responseObserver.onNext(version);
        responseObserver.onCompleted();
    }

    @Override
    public void unimplemented(final Empty request, final StreamObserver<SomeType> responseObserver) {
        log.debug("unimplemented");
        // Not implemented (on purpose)
        super.unimplemented(request, responseObserver);
    }

    @Override
    public void error(final Empty request, final StreamObserver<Empty> responseObserver) {
        log.debug("error");
        responseObserver.onError(Status.INTERNAL.asRuntimeException());
    }

    @Override
    public StreamObserver<SomeType> echo(final StreamObserver<SomeType> responseObserver) {
        log.debug("echo");
        return responseObserver;
    }

    @Override
    @Secured("ROLE_CLIENT1")
    public void secure(final Empty request, final StreamObserver<SomeType> responseObserver) {
        final Authentication authentication = assertAuthenticated("secure");

        assertSameAuthenticatedGrcContextCancellation("secure-cancellation", authentication);

        final SomeType version = SomeType.newBuilder().setVersion("1.2.3").build();
        responseObserver.onNext(version);
        responseObserver.onCompleted();
    }

    @Override
    @Secured("ROLE_CLIENT1")
    public StreamObserver<SomeType> secureDrain(final StreamObserver<Empty> responseObserver) {
        final Authentication authentication = assertAuthenticated("secureDrain");

        assertSameAuthenticatedGrcContextCancellation("secureDrain-cancellation", authentication);

        return new StreamObserver<SomeType>() {

            @Override
            public void onNext(final SomeType input) {
                assertSameAuthenticated("secureDrain-onNext", authentication);
                assertEquals("1.2.3", input.getVersion());
            }

            @Override
            public void onError(final Throwable t) {
                assertSameAuthenticated("secureDrain-onError", authentication);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                assertSameAuthenticated("secureDrain-onCompleted", authentication);
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }

        };
    }

    @Override
    @Secured("ROLE_CLIENT1")
    public void secureSupply(final Empty request, final StreamObserver<SomeType> responseObserver) {
        final Authentication authentication = assertAuthenticated("secureListener");

        assertSameAuthenticatedGrcContextCancellation("secureSupply-cancellation", authentication);

        responseObserver.onNext(SomeType.newBuilder().setVersion("1.2.3").build());
        responseObserver.onNext(SomeType.newBuilder().setVersion("1.2.3").build());
        responseObserver.onNext(SomeType.newBuilder().setVersion("1.2.3").build());
        responseObserver.onCompleted();
    }

    @Override
    @Secured("ROLE_CLIENT1")
    public StreamObserver<SomeType> secureBidi(final StreamObserver<SomeType> responseObserver) {
        final Authentication authentication = assertAuthenticated("secureBidi");

        assertSameAuthenticatedGrcContextCancellation("secureBidi-cancellation", authentication);

        return new StreamObserver<SomeType>() {

            @Override
            public void onNext(final SomeType input) {
                assertSameAuthenticated("secureBidi-onNext", authentication);
                assertEquals("1.2.3", input.getVersion());
                responseObserver.onNext(input);
            }

            @Override
            public void onError(final Throwable t) {
                assertSameAuthenticated("secureBidi-onError", authentication);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                assertSameAuthenticated("secureBidi-onCompleted", authentication);
                responseObserver.onCompleted();
            }

        };
    }

    protected Authentication assertAuthenticated(final String method) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return assertAuthenticated(method, authentication);
    }

    protected Authentication assertAuthenticated(final String method, final Authentication actual) {
        assertNotNull(actual, "No user authentication");
        assertTrue(actual.isAuthenticated(), "User not authenticated!");
        log.debug("{}: {}", method, actual.getName());
        return actual;
    }

    protected void assertSameAuthenticatedGrcContextCancellation(final String method, final Authentication expected) {
        Context.current().addListener(context -> {
            assertSameAuthenticatedGrcContextOnly(method, expected, context);
        }, MoreExecutors.directExecutor());
    }

    protected Authentication assertSameAuthenticatedGrcContextOnly(final String method, final Authentication expected,
            final Context context) {
        return assertSameAuthenticated(method, expected,
                AuthenticatingServerInterceptor.SECURITY_CONTEXT_KEY.get(context).getAuthentication());
    }

    protected Authentication assertSameAuthenticated(final String method, final Authentication expected) {
        assertSameAuthenticatedGrcContextOnly(method, expected, Context.current());
        return assertSameAuthenticated(method, expected, SecurityContextHolder.getContext().getAuthentication());
    }

    protected Authentication assertSameAuthenticated(final String method, final Authentication expected,
            final Authentication actual) {
        assertSame(expected, actual, method);
        return assertAuthenticated(method, expected);
    }

}

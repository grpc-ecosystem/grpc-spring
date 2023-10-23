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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import io.grpc.Status;

/**
 * Tests for {@link GrpcAdviceDiscoverer}.
 */
class GrpcAdviceDiscovererTest {

    private final ApplicationContext context = mock(ApplicationContext.class);

    @BeforeEach
    void beforeEach() {
        reset(this.context);
    }

    /**
     * Tests that the {@link GrpcAdviceDiscoverer} discovers inherited methods.
     */
    @Test
    void testDiscoversInheritedMethods() {
        when(this.context.getBeansWithAnnotation(GrpcAdvice.class))
                .thenReturn(singletonMap("bean", new Extended()));

        final GrpcAdviceDiscoverer disco = new GrpcAdviceDiscoverer();
        disco.setApplicationContext(this.context);
        disco.afterPropertiesSet();

        assertThat(disco.getAnnotatedMethods())
                .containsExactlyInAnyOrder(
                        findMethod(Base.class, "handleRuntimeException", RuntimeException.class),
                        findMethod(Extended.class, "handleIllegalArgument", IllegalArgumentException.class));
    }

    @Test
    void testOverriddenMethods() {
        when(this.context.getBeansWithAnnotation(GrpcAdvice.class))
                .thenReturn(singletonMap("bean", new Overriden()));

        final GrpcAdviceDiscoverer disco = new GrpcAdviceDiscoverer();
        disco.setApplicationContext(this.context);
        disco.afterPropertiesSet();

        assertThat(disco.getAnnotatedMethods())
                .containsExactly(findMethod(Overriden.class, "handleRuntimeException", RuntimeException.class));
    }

    private static Method findMethod(final Class<?> clazz, final String method, final Class<?>... parameters) {
        try {
            return clazz.getDeclaredMethod(method, parameters);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Failed to find method", e);
        }
    }

    @GrpcAdvice
    private class Base {

        @GrpcExceptionHandler
        Status handleRuntimeException(final RuntimeException e) {
            return Status.INTERNAL;
        }

    }

    @GrpcAdvice
    private class Extended extends Base {

        @GrpcExceptionHandler
        Status handleIllegalArgument(final IllegalArgumentException e) {
            return Status.INVALID_ARGUMENT;
        }

    }

    @GrpcAdvice
    private class Overriden extends Base {

        @Override
        @GrpcExceptionHandler
        Status handleRuntimeException(final RuntimeException e) {
            return Status.INVALID_ARGUMENT;
        }

    }

}

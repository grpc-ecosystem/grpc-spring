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

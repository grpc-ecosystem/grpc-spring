/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.util.concurrent.ListenableFuture;

public final class FutureAssertions {

    public static <T> void assertFutureEquals(final T expected, final ListenableFuture<T> future,
            final int timeout, final TimeUnit timeoutUnit) {
        assertFutureEquals(expected, future, UnaryOperator.identity(), timeout, timeoutUnit);
    }

    public static <T, R> void assertFutureEquals(final T expected, final ListenableFuture<R> future,
            final Function<R, T> unwrapper, final int timeout, final TimeUnit timeoutUnit) {
        try {
            assertEquals(expected, unwrapper.apply(future.get(timeout, timeoutUnit)));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Exception> T assertFutureThrows(final Class<T> expectedType,
            final ListenableFuture<?> future, final int timeout, final TimeUnit timeoutUnit) {
        final Throwable cause =
                assertThrows(ExecutionException.class, () -> future.get(timeout, timeoutUnit)).getCause();
        final Class<? extends Throwable> causeClass = cause.getClass();
        assertTrue(expectedType.isAssignableFrom(causeClass), "The cause was of type: " + causeClass.getName()
                + ", but it was expected to be a subclass of " + expectedType.getName());
        return (T) cause;
    }

    private FutureAssertions() {}

}

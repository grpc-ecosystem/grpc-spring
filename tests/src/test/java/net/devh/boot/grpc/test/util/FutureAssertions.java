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

package net.devh.boot.grpc.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Assertions related to {@link Future}s.
 */
public final class FutureAssertions {

    /**
     * Asserts that the {@link Future} returns the expected result.
     *
     * @param <T> The type of the future content.
     * @param expected The expected content.
     * @param future The future to check for the expected content.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     */
    public static <T> void assertFutureEquals(final T expected, final Future<T> future,
            final int timeout, final TimeUnit timeoutUnit) {
        assertFutureEquals(expected, future, UnaryOperator.identity(), timeout, timeoutUnit);
    }

    /**
     * Asserts that the {@link Future} returns the expected result.
     *
     * @param <T> The type of the unwrapped/expected content.
     * @param <R> The type of the future content.
     * @param expected The expected content.
     * @param future The future to check for the expected content.
     * @param unwrapper The function used to extract the content.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     */
    public static <T, R> void assertFutureEquals(final T expected, final Future<R> future,
            final Function<R, T> unwrapper, final int timeout, final TimeUnit timeoutUnit) {
        try {
            assertEquals(expected, unwrapper.apply(future.get(timeout, timeoutUnit)));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Unexpected error while trying to get the result", e);
        }
    }

    /**
     * Asserts that the given {@link Future} fails with an {@link ExecutionException} caused by the given exception
     * type.
     *
     * @param <T> The type of the causing exception.
     * @param expectedType The expected type of the causing exception.
     * @param future The future expected to throw.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     * @return The causing exception.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Exception> T assertFutureThrows(final Class<T> expectedType,
            final Future<?> future, final int timeout, final TimeUnit timeoutUnit) {
        final Throwable cause =
                assertThrows(ExecutionException.class, () -> future.get(timeout, timeoutUnit)).getCause();
        final Class<? extends Throwable> causeClass = cause.getClass();
        assertTrue(expectedType.isAssignableFrom(causeClass), "The cause was of type: " + causeClass.getName()
                + ", but it was expected to be a subclass of " + expectedType.getName());
        return (T) cause;
    }

    private FutureAssertions() {}

}

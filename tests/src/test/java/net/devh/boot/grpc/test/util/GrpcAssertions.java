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

import static net.devh.boot.grpc.test.util.FutureAssertions.assertFutureEquals;
import static net.devh.boot.grpc.test.util.FutureAssertions.assertFutureThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.testing.StreamRecorder;

/**
 * Assertions related to gRPC client calls.
 */
public final class GrpcAssertions {

    /**
     * Asserts that the first value in the {@link StreamRecorder} equals the expected value.
     *
     * @param <T> The type of the observer's content.
     * @param expected The expected content.
     * @param responseObserver The observer to check for the expected content.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     */
    public static <T> void assertFutureFirstEquals(final T expected, final StreamRecorder<T> responseObserver,
            final int timeout, final TimeUnit timeoutUnit) {
        assertFutureFirstEquals(expected, responseObserver, UnaryOperator.identity(), timeout, timeoutUnit);
    }

    /**
     * Asserts that the first value in the {@link StreamRecorder} equals the expected value.
     *
     * @param <T> The type of the unwrapped/expected content.
     * @param <R> The type of the observer's content.
     * @param expected The expected content.
     * @param responseObserver The observer to check for the expected content.
     * @param unwrapper The function used to extract the content.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     */
    public static <T, R> void assertFutureFirstEquals(final T expected, final StreamRecorder<R> responseObserver,
            final Function<R, T> unwrapper, final int timeout, final TimeUnit timeoutUnit) {
        assertFutureEquals(expected, responseObserver.firstValue(), unwrapper, timeout, timeoutUnit);
    }

    /**
     * Assert that the given {@link Executable} throws a {@link StatusRuntimeException}.
     *
     * @param executable The executable to run.
     * @return The thrown exception.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static StatusRuntimeException assertThrowsStatus(final Executable executable) {
        return assertThrows(StatusRuntimeException.class, executable);
    }

    /**
     * Assert that the given {@link Executable} throws a {@link StatusRuntimeException} with the expected status code.
     *
     * @param expectedCode The expected status code.
     * @param executable The executable to run.
     * @return The status contained in the exception.
     * @see Assertions#assertThrows(Class, Executable)
     */
    public static Status assertThrowsStatus(final Status.Code expectedCode, final Executable executable) {
        final StatusRuntimeException exception = assertThrowsStatus(executable);
        return assertStatus(expectedCode, exception);
    }

    /**
     * Asserts that the given {@link StreamRecorder} throws an {@link ExecutionException} caused by a
     * {@link StatusRuntimeException} with the expected status code.
     *
     * @param expectedCode The expected status code.
     * @param recorder The recorder expected to throw.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     * @return The status contained in the exception.
     * @see #assertFutureThrowsStatus(io.grpc.Status.Code, Future, int, TimeUnit)
     */
    public static Status assertFutureThrowsStatus(final Status.Code expectedCode, final StreamRecorder<?> recorder,
            final int timeout, final TimeUnit timeoutUnit) {
        return assertFutureThrowsStatus(expectedCode, recorder.firstValue(), timeout, timeoutUnit);
    }

    /**
     * Asserts that the given {@link Future} throws an {@link ExecutionException} caused by a
     * {@link StatusRuntimeException} with the expected status code.
     *
     * @param future The future expected to throw.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     * @return The thrown StatusRuntimeException.
     */
    public static StatusRuntimeException assertFutureThrowsStatus(final Future<?> future,
            final int timeout, final TimeUnit timeoutUnit) {
        return assertFutureThrows(StatusRuntimeException.class, future, timeout, timeoutUnit);
    }

    /**
     * Asserts that the given {@link Future} throws an {@link ExecutionException} caused by a
     * {@link StatusRuntimeException} with the expected status code.
     *
     * @param expectedCode The expected status code.
     * @param future The future expected to throw.
     * @param timeout The maximum time to wait for the result.
     * @param timeoutUnit The time unit of the {@code timeout} argument.
     * @return The status contained in the exception.
     */
    public static Status assertFutureThrowsStatus(final Status.Code expectedCode, final Future<?> future,
            final int timeout, final TimeUnit timeoutUnit) {
        final StatusRuntimeException exception = assertFutureThrowsStatus(future, timeout, timeoutUnit);
        return assertStatus(expectedCode, exception);
    }

    /**
     * Asserts that the given {@link StatusRuntimeException} uses the expected status code.
     *
     * @param expectedCode The expected status code.
     * @param exception The exception to check for the status code.
     * @return The status contained in the exception.
     */
    public static Status assertStatus(final Status.Code expectedCode, final StatusRuntimeException exception) {
        final Status status = exception.getStatus();
        assertEquals(expectedCode, status.getCode());
        return status;
    }

    private GrpcAssertions() {}

}

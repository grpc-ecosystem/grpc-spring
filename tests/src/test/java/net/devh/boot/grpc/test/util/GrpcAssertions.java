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

package net.devh.boot.grpc.test.util;

import static net.devh.boot.grpc.test.util.FutureAssertions.assertFutureEquals;
import static net.devh.boot.grpc.test.util.FutureAssertions.assertFutureThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.function.Executable;

import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.testing.StreamRecorder;

public final class GrpcAssertions {

    public static <T> void assertFutureFirstEquals(final T expected, final StreamRecorder<T> responseObserver,
            final int timeout, final TimeUnit timeoutUnit) {
        assertFutureFirstEquals(expected, responseObserver, UnaryOperator.identity(), timeout, timeoutUnit);
    }

    public static <T, R> void assertFutureFirstEquals(final T expected, final StreamRecorder<R> responseObserver,
            final Function<R, T> unwrapper, final int timeout, final TimeUnit timeoutUnit) {
        assertFutureEquals(expected, responseObserver.firstValue(), unwrapper, timeout, timeoutUnit);
    }

    public static Status assertThrowsStatus(final Status.Code code, final Executable executable) {
        final StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, executable);
        return assertStatus(code, exception);
    }

    public static Status assertFutureThrowsStatus(final Status.Code code, final StreamRecorder<?> recorder,
            final int timeout, final TimeUnit timeoutUnit) {
        return assertFutureThrowsStatus(code, recorder.firstValue(), timeout, timeoutUnit);
    }

    public static Status assertFutureThrowsStatus(final Status.Code code, final ListenableFuture<?> future,
            final int timeout, final TimeUnit timeoutUnit) {
        final StatusRuntimeException exception =
                assertFutureThrows(StatusRuntimeException.class, future, timeout, timeoutUnit);
        return assertStatus(code, exception);
    }

    public static Status assertStatus(final Status.Code code, final StatusRuntimeException exception) {
        final Status status = exception.getStatus();
        assertEquals(code, status.getCode());
        return status;
    }

    private GrpcAssertions() {}

}

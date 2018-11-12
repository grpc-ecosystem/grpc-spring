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

package net.devh.test.grpc.util;

import static net.devh.test.grpc.util.FutureAssertions.assertFutureThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.function.Executable;

import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class GrpcAssertions {

    public static Status assertThrowsStatus(final Status.Code code, final Executable executable) {
        final StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, executable);
        return assertStatus(code, exception);
    }

    public static Status assertFutureThrowsStatus(final Status.Code code, final ListenableFuture<?> future, int timeout,
            TimeUnit timeoutUnit) {
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

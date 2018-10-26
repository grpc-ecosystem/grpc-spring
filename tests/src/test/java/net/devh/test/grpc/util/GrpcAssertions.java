package net.devh.test.grpc.util;

import static net.devh.test.grpc.util.FutureAssertions.assertFutureThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class GrpcAssertions {

    public static Status assertThrowsStatus(final Status.Code code, final Executable executable) {
        final StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, executable);
        return assertStatus(code, exception);
    }

    public static Status assertFutureThrowsStatus(final Status.Code code, final ListenableFuture<?> future) {
        final StatusRuntimeException exception = assertFutureThrows(StatusRuntimeException.class, future);
        return assertStatus(code, exception);
    }

    public static Status assertStatus(final Status.Code code, final StatusRuntimeException exception) {
        final Status status = exception.getStatus();
        assertEquals(code, status.getCode());
        return status;
    }

    private GrpcAssertions() {}

}

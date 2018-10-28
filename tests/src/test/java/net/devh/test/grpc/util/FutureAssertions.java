package net.devh.test.grpc.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListenableFuture;

public final class FutureAssertions {

    @SuppressWarnings("unchecked")
    public static <T extends Exception> T assertFutureThrows(final Class<T> expectedType,
            final ListenableFuture<?> future) {
        final Throwable cause =
                assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS)).getCause();
        final Class<? extends Throwable> causeClass = cause.getClass();
        assertTrue(expectedType.isAssignableFrom(causeClass), "The cause was of type: " + causeClass.getName()
                + ", but it was expected to be a subclass of " + expectedType.getName());
        return (T) cause;
    }

    private FutureAssertions() {}

}

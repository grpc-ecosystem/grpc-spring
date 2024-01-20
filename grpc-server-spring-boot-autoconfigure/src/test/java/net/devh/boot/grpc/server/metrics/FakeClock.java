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

package net.devh.boot.grpc.server.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;

/**
 * A manipulated clock that exports a {@link com.google.common.base.Ticker}.
 */
public final class FakeClock {
    private long currentTimeNanos;
    private final Ticker ticker =
            new Ticker() {
                @Override
                public long read() {
                    return currentTimeNanos;
                }
            };

    private final Supplier<Stopwatch> stopwatchSupplier =
            new Supplier<Stopwatch>() {
                @Override
                public Stopwatch get() {
                    return Stopwatch.createUnstarted(ticker);
                }
            };

    /**
     * Forward the time by the given duration.
     */
    public void forwardTime(long value, TimeUnit unit) {
        currentTimeNanos += unit.toNanos(value);
        return;
    }

    /**
     * Provides a stopwatch instance that uses the fake clock ticker.
     */
    public Supplier<Stopwatch> getStopwatchSupplier() {
        return stopwatchSupplier;
    }
}

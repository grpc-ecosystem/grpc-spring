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

package net.devh.boot.grpc.client.metrics;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.atomic.AtomicLong;

import io.grpc.ClientStreamTracer;
import io.grpc.ClientStreamTracer.StreamInfo;
import io.grpc.Metadata;
import io.micrometer.core.instrument.Tags;

/**
 * Provides factories for {@link io.grpc.StreamTracer} that records metrics.
 *
 * <p>
 * On the client-side, a factory is created for each call, and the factory creates a stream tracer for each attempt.
 */
public class MetricsClientStreamTracers {

    private static final class ClientTracer extends ClientStreamTracer {
        private final CallAttemptsTracerFactory attemptsState;
        private final StreamInfo info;
        private final String fullMethodName;

        ClientTracer(CallAttemptsTracerFactory attemptsState, StreamInfo info, String fullMethodName) {
            this.attemptsState = attemptsState;
            this.info = info;
            this.fullMethodName = fullMethodName;
        }

    }

    static final class CallAttemptsTracerFactory extends ClientStreamTracer.Factory {
        private final String fullMethodName;
        private final MetricsMeters metricsMeters;
        private final AtomicLong attemptsPerCall = new AtomicLong();

        CallAttemptsTracerFactory(String fullMethodName,
                final MetricsMeters metricsMeters) {
            this.fullMethodName = checkNotNull(fullMethodName, "fullMethodName");
            this.metricsMeters = checkNotNull(metricsMeters, "metricsMeters");

            // Record here in case newClientStreamTracer() would never be called.
            this.metricsMeters.getAttemptCounter()
                    .withTags(Tags.of("grpc.method", fullMethodName))
                    .increment();
        }

        @Override
        public ClientStreamTracer newClientStreamTracer(StreamInfo info, Metadata metadata) {
            if (attemptsPerCall.get() > 0) {
                this.metricsMeters.getAttemptCounter()
                        .withTags((Tags.of("grpc.method", fullMethodName)))
                        .increment();
            }
            if (!info.isTransparentRetry()) {
                attemptsPerCall.incrementAndGet();
            }
            return new ClientTracer(this, info, fullMethodName);
        }

    }
}

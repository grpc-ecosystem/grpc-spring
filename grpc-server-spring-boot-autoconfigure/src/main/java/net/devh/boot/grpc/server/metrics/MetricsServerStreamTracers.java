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

import io.grpc.Metadata;
import io.grpc.ServerStreamTracer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public final class MetricsServerStreamTracers {

    public MetricsServerStreamTracers() {}

    private static final class ServerTracer extends ServerStreamTracer {
        private final String fullMethodName;
        private final MetricsServerMeters metricsServerMeters;

        ServerTracer(String fullMethodName, MetricsServerMeters meters) {
            this.fullMethodName = fullMethodName;
            this.metricsServerMeters = meters;
            this.metricsServerMeters.getServerCallCounter()
                    .withTags(Tags.of("grpc.method", this.fullMethodName))
                    .increment();
        }

    }

    final class MetricsServerTracerFactory extends ServerStreamTracer.Factory {

        private final MetricsServerMeters metricsServerMeters;

        MetricsServerTracerFactory(MeterRegistry registry) {
            this.metricsServerMeters = MetricsServerInstruments.newServerMetricsMeters(registry);
        }

        @Override
        public ServerStreamTracer newServerStreamTracer(String fullMethodName, Metadata headers) {
            return new ServerTracer(fullMethodName, this.metricsServerMeters);
        }
    }


    // Concurrent?
    public ServerStreamTracer.Factory getMetricsServerTracerFactory(MeterRegistry registry) {
        return new MetricsServerTracerFactory(registry);
    }

}

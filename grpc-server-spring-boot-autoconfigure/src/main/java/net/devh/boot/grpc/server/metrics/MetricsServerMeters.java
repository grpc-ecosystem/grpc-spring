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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter.MeterProvider;

/*
 * Collection of metrics meters.
 */
public class MetricsServerMeters {

    private MeterProvider<Counter> serverCallCounter;

    private MetricsServerMeters(Builder builder) {
        this.serverCallCounter = builder.serverCallCounter;
    }

    public MeterProvider<Counter> getServerCallCounter() {
        return this.serverCallCounter;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static class Builder {

        private MeterProvider<Counter> serverCallCounter;

        private Builder() {}

        public Builder setServerCallCounter(MeterProvider<Counter> counter) {
            this.serverCallCounter = counter;
            return this;
        }

        public MetricsServerMeters build() {
            return new MetricsServerMeters(this);
        }
    }
}

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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter.MeterProvider;
import io.micrometer.core.instrument.Timer;

/*
 * Collection of client metrics meters.
 */
public class MetricsClientMeters {

    private MeterProvider<Counter> attemptCounter;
    private MeterProvider<DistributionSummary> sentMessageSizeDistribution;
    private MeterProvider<DistributionSummary> receivedMessageSizeDistribution;
    private MeterProvider<Timer> clientAttemptDuration;
    private MeterProvider<Timer> clientCallDuration;

    private MetricsClientMeters(Builder builder) {
        this.attemptCounter = builder.attemptCounter;
        this.sentMessageSizeDistribution = builder.sentMessageSizeDistribution;
        this.receivedMessageSizeDistribution = builder.receivedMessageSizeDistribution;
        this.clientAttemptDuration = builder.clientAttemptDuration;
        this.clientCallDuration = builder.clientCallDuration;
    }

    public MeterProvider<Counter> getAttemptCounter() {
        return this.attemptCounter;
    }

    public MeterProvider<DistributionSummary> getSentMessageSizeDistribution() {
        return this.sentMessageSizeDistribution;
    }

    public MeterProvider<DistributionSummary> getReceivedMessageSizeDistribution() {
        return this.receivedMessageSizeDistribution;
    }

    public MeterProvider<Timer> getClientAttemptDuration() {
        return this.clientAttemptDuration;
    }

    public MeterProvider<Timer> getClientCallDuration() {
        return this.clientCallDuration;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static class Builder {

        private MeterProvider<Counter> attemptCounter;
        private MeterProvider<DistributionSummary> sentMessageSizeDistribution;
        private MeterProvider<DistributionSummary> receivedMessageSizeDistribution;
        private MeterProvider<Timer> clientAttemptDuration;
        private MeterProvider<Timer> clientCallDuration;

        private Builder() {}

        public Builder setAttemptCounter(MeterProvider<Counter> counter) {
            this.attemptCounter = counter;
            return this;
        }

        public Builder setSentMessageSizeDistribution(MeterProvider<DistributionSummary> distribution) {
            this.sentMessageSizeDistribution = distribution;
            return this;
        }

        public Builder setReceivedMessageSizeDistribution(MeterProvider<DistributionSummary> distribution) {
            this.receivedMessageSizeDistribution = distribution;
            return this;
        }

        public Builder setClientAttemptDuration(MeterProvider<Timer> timer) {
            this.clientAttemptDuration = timer;
            return this;
        }

        public Builder setClientCallDuration(MeterProvider<Timer> timer) {
            this.clientCallDuration = timer;
            return this;
        }

        public MetricsClientMeters build() {
            return new MetricsClientMeters(this);
        }
    }
}

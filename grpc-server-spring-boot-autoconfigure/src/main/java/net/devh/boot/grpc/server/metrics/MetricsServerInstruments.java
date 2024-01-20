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

import java.time.Duration;
import java.util.stream.DoubleStream;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;

/*
 * The instruments used to record metrics on server.
 */
public final class MetricsServerInstruments {

    protected MetricsServerInstruments() {}

    /*
     * Server side metrics defined in gRFC <a
     * href="https://github.com/grpc/proposal/blob/master/A66-otel-stats.md">A66</a>. Please note that these are the
     * names used for instrumentation and can be changed by exporters in an unpredictable manner depending on the
     * destination.
     */
    private static final String SERVER_CALL_STARTED = "grpc.server.call.started";
    private static final String SERVER_SENT_COMPRESSED_MESSAGE_SIZE =
            "grpc.server.call.sent_total_compressed_message_size";
    private static final String SERVER_RECEIVED_COMPRESSED_MESSAGE_SIZE =
            "grpc.server.call.rcvd_total_compressed_message_size";
    private static final String SERVER_CALL_DURATION =
            "grpc.server.call.duration";
    private static final double[] DEFAULT_SIZE_BUCKETS =
            DoubleStream.of(1024d, 2048d, 4096d, 16384d, 65536d, 262144d, 1048576d,
                    4194304d, 16777216d, 67108864d, 268435456d, 1073741824d, 4294967296d).toArray();
    private static final Duration[] DEFAULT_LATENCY_BUCKETS =
            new Duration[] {Duration.ofNanos(10000), Duration.ofNanos(50000), Duration.ofNanos(100000),
                    Duration.ofNanos(300000), Duration.ofNanos(600000), Duration.ofNanos(800000),
                    Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3), Duration.ofMillis(4),
                    Duration.ofMillis(5), Duration.ofMillis(6), Duration.ofMillis(8), Duration.ofMillis(10),
                    Duration.ofMillis(13), Duration.ofMillis(16), Duration.ofMillis(20), Duration.ofMillis(25),
                    Duration.ofMillis(30), Duration.ofMillis(40), Duration.ofMillis(50), Duration.ofMillis(65),
                    Duration.ofMillis(80), Duration.ofMillis(100), Duration.ofMillis(130), Duration.ofMillis(160),
                    Duration.ofMillis(200), Duration.ofMillis(250), Duration.ofMillis(300), Duration.ofMillis(400),
                    Duration.ofMillis(500), Duration.ofMillis(650), Duration.ofMillis(800),
                    Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(5), Duration.ofSeconds(10),
                    Duration.ofSeconds(20), Duration.ofSeconds(50), Duration.ofSeconds(100)};

    static MetricsServerMeters newServerMetricsMeters(MeterRegistry registry) {
        MetricsServerMeters.Builder builder = MetricsServerMeters.newBuilder();

        builder.setServerCallCounter(Counter.builder(SERVER_CALL_STARTED)
                .description("The total number of RPC attempts started from the server side, including "
                        + "those that have not completed.")
                .baseUnit("call")
                .withRegistry(registry));

        builder.setSentMessageSizeDistribution(DistributionSummary.builder(
                SERVER_SENT_COMPRESSED_MESSAGE_SIZE)
                .description("Compressed message bytes sent per server call")
                .baseUnit(BaseUnits.BYTES)
                .serviceLevelObjectives(DEFAULT_SIZE_BUCKETS)
                .withRegistry(registry));

        builder.setReceivedMessageSizeDistribution(DistributionSummary.builder(
                SERVER_RECEIVED_COMPRESSED_MESSAGE_SIZE)
                .description("Compressed message bytes received per server call")
                .baseUnit(BaseUnits.BYTES)
                .serviceLevelObjectives(DEFAULT_SIZE_BUCKETS)
                .withRegistry(registry));

        builder.setServerCallDuration(Timer.builder(SERVER_CALL_DURATION)
                .description("Time taken to complete a call from server transport's perspective")
                .serviceLevelObjectives(DEFAULT_LATENCY_BUCKETS)
                .withRegistry(registry));

        return builder.build();
    }
}

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
import io.micrometer.core.instrument.MeterRegistry;

/*
 * The instruments used to record metrics on client.
 */
public final class MetricsClientInstruments {

    private MetricsClientInstruments() {}

    /*
     * This is a client side metric defined in gRFC <a
     * href="https://github.com/grpc/proposal/blob/master/A66-otel-stats.md">A66</a>. Please note that this is the name
     * used for instrumentation and can be changed by exporters in an unpredictable manner depending on the destination.
     */
    private static final String CLIENT_ATTEMPT_STARTED = "grpc.client.attempt.started";

    static MetricsMeters newClientMetricsMeters(MeterRegistry registry) {
        MetricsMeters.Builder builder = MetricsMeters.newBuilder();

        builder.setAttemptCounter(Counter.builder(CLIENT_ATTEMPT_STARTED)
                .description(
                        "The total number of RPC attempts started from the client side, including "
                                + "those that have not completed.")
                .baseUnit("attempt")
                .withRegistry(registry));
        return builder.build();
    }

}

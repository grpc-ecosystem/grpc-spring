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

package net.devh.boot.grpc.client.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class MetricsClientInstruments {

    static final String CLIENT_ATTEMPT_STARTED = "grpc.client.attempt.started";

    static MetricsCounters micrometerInstruments(MeterRegistry registry) {
        MetricsCounters.Builder builder = MetricsCounters.newBuilder();

        builder.setAttemptCounter(Counter.builder(CLIENT_ATTEMPT_STARTED)
                .description("The total number of RPC attempts started, including those that have not completed.")
                .baseUnit("attempt")
                .withRegistry(registry));

        return builder.build();
    }


}

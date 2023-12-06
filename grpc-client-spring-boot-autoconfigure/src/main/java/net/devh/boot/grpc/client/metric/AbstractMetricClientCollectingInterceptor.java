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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;

public abstract class AbstractMetricClientCollectingInterceptor {
    private static final String TAG_METHOD_NAME = "method";

    protected static Counter.Builder prepareCounterFor(final MethodDescriptor<?, ?> method, final String name,
            final String description) {
        return Counter.builder(name)
                .description(description)
                .baseUnit(BaseUnits.MESSAGES)
                .tag(TAG_METHOD_NAME, method.getBareMethodName());
    }

    private final Map<MethodDescriptor<?, ?>, MetricSet> metricsForMethods = new ConcurrentHashMap<>();

    protected final MeterRegistry registry;

    protected final UnaryOperator<Counter.Builder> counterCustomizer;

    protected final UnaryOperator<Timer.Builder> timerCustomizer;

    protected final Status.Code[] eagerInitializedCodes;

    protected AbstractMetricClientCollectingInterceptor(final MeterRegistry registry) {
        this(registry, UnaryOperator.identity(), UnaryOperator.identity(), Code.OK);
    }

    protected AbstractMetricClientCollectingInterceptor(final MeterRegistry registry,
            final UnaryOperator<Counter.Builder> counterCustomizer, final UnaryOperator<Timer.Builder> timerCustomizer,
            final Status.Code... eagerInitializedCodes) {
        this.registry = registry;
        this.counterCustomizer = counterCustomizer;
        this.timerCustomizer = timerCustomizer;
        this.eagerInitializedCodes = eagerInitializedCodes;
    }

    public void preregisterService(final ServiceDescriptor service) {
        for (final MethodDescriptor<?, ?> method : service.getMethods()) {
            preregisterMethod(method);
        }
    }

    public void preregisterMethod(final MethodDescriptor<?, ?> method) {
        metricsFor(method);
    }

    protected final MetricSet metricsFor(final MethodDescriptor<?, ?> method) {
        return this.metricsForMethods.computeIfAbsent(method, this::newMetricsFor);
    }

    protected MetricSet newMetricsFor(final MethodDescriptor<?, ?> method) {
        return new MetricSet(newAttemptCounterFor(method));
    }

    protected abstract Counter newAttemptCounterFor(final MethodDescriptor<?, ?> method);

    protected static class MetricSet {

        private final Counter attemptCounter;

        public MetricSet(final Counter requestCounter) {
            this.attemptCounter = requestCounter;
        }

        public Counter getAttemptCounter() {
            return this.attemptCounter;
        }

    }

}

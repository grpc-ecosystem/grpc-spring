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

package net.devh.boot.grpc.server.autoconfigure;

import static net.devh.boot.grpc.common.util.GrpcUtils.extractMethodName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.SimpleInfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

import com.google.common.base.Stopwatch;

import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.metrics.MetricsServerStreamTracers;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

/**
 * Auto configuration class for Spring-Boot. This allows zero config server metrics for gRPC services.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(CompositeMeterRegistryAutoConfiguration.class)
@AutoConfigureBefore(GrpcServerAutoConfiguration.class)
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnClass(MetricCollectingServerInterceptor.class)
public class GrpcServerMetricAutoConfiguration {

    @GrpcGlobalServerInterceptor
    @Order(InterceptorOrder.ORDER_TRACING_METRICS)
    @ConditionalOnMissingBean
    public MetricCollectingServerInterceptor metricCollectingServerInterceptor(final MeterRegistry registry,
            final Collection<BindableService> services) {
        final MetricCollectingServerInterceptor metricCollector = new MetricCollectingServerInterceptor(registry);
        log.debug("Pre-Registering service metrics");
        for (final BindableService service : services) {
            log.debug("- {}", service);
            metricCollector.preregisterService(service);
        }
        return metricCollector;
    }

    @ConditionalOnProperty(prefix = "grpc", name = "metricsA66Enabled", matchIfMissing = true)
    @Bean
    public GrpcServerConfigurer streamTracerFactoryConfigurer(final MeterRegistry registry) {
        MetricsServerStreamTracers metricsServerStreamTracers = new MetricsServerStreamTracers(
                Stopwatch::createUnstarted);
        return builder -> builder
                .addStreamTracerFactory(metricsServerStreamTracers.getMetricsServerTracerFactory(registry));
    }

    @Bean
    @Lazy
    InfoContributor grpcInfoContributor(final GrpcServerProperties properties,
            final Collection<BindableService> grpcServices) {
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("port", properties.getPort());

        if (properties.isReflectionServiceEnabled()) {
            // Only expose services via web-info if we do the same via grpc.
            final Map<String, List<String>> services = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            details.put("services", services);
            for (final BindableService grpcService : grpcServices) {
                final ServiceDescriptor serviceDescriptor = grpcService.bindService().getServiceDescriptor();

                final List<String> methods = collectMethodNamesForService(serviceDescriptor);
                services.put(serviceDescriptor.getName(), methods);
            }
        }

        return new SimpleInfoContributor("grpc.server", details);
    }

    /**
     * Gets all method names from the given service descriptor.
     *
     * @param serviceDescriptor The service descriptor to get the names from.
     * @return The newly created and sorted list of the method names.
     */
    protected List<String> collectMethodNamesForService(final ServiceDescriptor serviceDescriptor) {
        final List<String> methods = new ArrayList<>();
        for (final MethodDescriptor<?, ?> grpcMethod : serviceDescriptor.getMethods()) {
            methods.add(extractMethodName(grpcMethod));
        }
        methods.sort(String.CASE_INSENSITIVE_ORDER);
        return methods;
    }

}

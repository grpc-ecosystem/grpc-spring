/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.services.HealthStatusManager;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.metric.MetricCollectingServerInterceptor;

/**
 * Auto configuration class for Spring-Boot. This allows zero config server metrics for gRPC services.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@Configuration
@AutoConfigureAfter(CompositeMeterRegistryAutoConfiguration.class)
@AutoConfigureBefore(GrpcServerAutoConfiguration.class)
@ConditionalOnBean(MeterRegistry.class)
public class GrpcServerMetricAutoConfiguration {

    @Bean
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

    @Bean
    @Lazy
    InfoContributor grpcInfoContributor(final GrpcServerProperties properties,
            final Collection<BindableService> grpcServices, final HealthStatusManager healthStatusManager) {
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("port", properties.getPort());

        if (properties.isReflectionServiceEnabled()) {
            // Only expose services via web-info if we do the same via grpc.
            final Map<String, List<String>> services = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            details.put("services", services);
            final List<BindableService> mutableGrpcServiceList = new ArrayList<>(grpcServices);
            mutableGrpcServiceList.add(ProtoReflectionService.newInstance());
            if (properties.isHealthServiceEnabled()) {
                mutableGrpcServiceList.add(healthStatusManager.getHealthService());
            }
            for (final BindableService grpcService : mutableGrpcServiceList) {
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

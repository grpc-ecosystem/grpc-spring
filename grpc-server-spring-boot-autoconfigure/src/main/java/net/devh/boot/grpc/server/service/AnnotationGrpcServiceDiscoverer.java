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

package net.devh.boot.grpc.server.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;

/**
 * A {@link GrpcServiceDiscoverer} that searches for beans with the {@link GrpcService} annotations.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Slf4j
public class AnnotationGrpcServiceDiscoverer implements ApplicationContextAware, GrpcServiceDiscoverer {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<GrpcServiceDefinition> findGrpcServices() {
        Collection<String> beanNames =
                Arrays.asList(this.applicationContext.getBeanNamesForAnnotation(GrpcService.class));
        List<GrpcServiceDefinition> definitions = Lists.newArrayListWithCapacity(beanNames.size());
        GlobalServerInterceptorRegistry globalServerInterceptorRegistry =
                applicationContext.getBean(GlobalServerInterceptorRegistry.class);
        for (String beanName : beanNames) {
            BindableService bindableService = this.applicationContext.getBean(beanName, BindableService.class);
            ServerServiceDefinition serviceDefinition = bindableService.bindService();
            GrpcService grpcServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, GrpcService.class);
            serviceDefinition =
                    bindInterceptors(serviceDefinition, grpcServiceAnnotation, globalServerInterceptorRegistry);
            definitions.add(new GrpcServiceDefinition(beanName, bindableService.getClass(), serviceDefinition));
            log.debug("Found gRPC service: " + serviceDefinition.getServiceDescriptor().getName() + ", bean: "
                    + beanName + ", class: " + bindableService.getClass().getName());
        }
        return definitions;
    }

    private ServerServiceDefinition bindInterceptors(final ServerServiceDefinition serviceDefinition,
            final GrpcService grpcServiceAnnotation,
            final GlobalServerInterceptorRegistry globalServerInterceptorRegistry) {
        final List<ServerInterceptor> interceptors = Lists.newArrayList();
        interceptors.addAll(globalServerInterceptorRegistry.getServerInterceptors());
        for (final Class<? extends ServerInterceptor> interceptorClass : grpcServiceAnnotation.interceptors()) {
            final ServerInterceptor serverInterceptor;
            if (this.applicationContext.getBeanNamesForType(interceptorClass).length > 0) {
                serverInterceptor = this.applicationContext.getBean(interceptorClass);
            } else {
                try {
                    serverInterceptor = interceptorClass.getConstructor().newInstance();
                } catch (final Exception e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            interceptors.add(serverInterceptor);
        }
        for (final String interceptorName : grpcServiceAnnotation.interceptorNames()) {
            interceptors.add(this.applicationContext.getBean(interceptorName, ServerInterceptor.class));
        }
        if (grpcServiceAnnotation.sortInterceptors()) {
            globalServerInterceptorRegistry.sortInterceptors(interceptors);
        }
        return ServerInterceptors.interceptForward(serviceDefinition, interceptors);
    }

}

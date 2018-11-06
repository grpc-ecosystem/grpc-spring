/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.springboot.autoconfigure.grpc.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;

import io.grpc.BindableService;
import io.grpc.Codec;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodec;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

/**
 * A {@link GrpcServiceDiscoverer} that searches for beans with the {@link GrpcService} and {@link GrpcCodec}
 * annotations.
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
        List<ServerInterceptor> globalInterceptorList = globalServerInterceptorRegistry.getServerInterceptors();
        for (String beanName : beanNames) {
            BindableService bindableService = this.applicationContext.getBean(beanName, BindableService.class);
            ServerServiceDefinition serviceDefinition = bindableService.bindService();
            GrpcService grpcServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, GrpcService.class);
            serviceDefinition = bindInterceptors(serviceDefinition, grpcServiceAnnotation, globalInterceptorList);
            definitions.add(new GrpcServiceDefinition(beanName, bindableService.getClass(), serviceDefinition));
            log.debug("Found gRPC service: " + serviceDefinition.getServiceDescriptor().getName() + ", bean: "
                    + beanName + ", class: " + bindableService.getClass().getName());
        }
        return definitions;
    }

    @Override
    public Collection<GrpcCodecDefinition> findGrpcCodec() {
        Collection<String> beanNames =
                Arrays.asList(this.applicationContext.getBeanNamesForAnnotation(GrpcCodec.class));
        List<GrpcCodecDefinition> definitions = Lists.newArrayListWithCapacity(beanNames.size());
        for (String beanName : beanNames) {
            Codec codec = this.applicationContext.getBean(beanName, Codec.class);
            GrpcCodec annotation = applicationContext.findAnnotationOnBean(beanName, GrpcCodec.class);
            definitions.add(new GrpcCodecDefinition(codec, annotation.advertised(), annotation.codecType()));
            log.debug("Found custom gRPC custom codec: " + codec.getMessageEncoding() + ", bean: " + beanName
                    + ", class: " + codec.getClass().getName());
        }
        return definitions;
    }

    private ServerServiceDefinition bindInterceptors(final ServerServiceDefinition serviceDefinition,
            final GrpcService grpcServiceAnnotation, final List<ServerInterceptor> globalInterceptors) {
        final List<ServerInterceptor> interceptors = Lists.newArrayList();
        interceptors.addAll(globalInterceptors);
        for (final Class<? extends ServerInterceptor> interceptorClass : grpcServiceAnnotation.interceptors()) {
            final ServerInterceptor serverInterceptor;
            if (this.applicationContext.getBeanNamesForType(interceptorClass).length > 0) {
                serverInterceptor = this.applicationContext.getBean(interceptorClass);
            } else {
                try {
                    serverInterceptor = interceptorClass.newInstance();
                } catch (final Exception e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            interceptors.add(serverInterceptor);
        }
        for (final String interceptorName : grpcServiceAnnotation.interceptorNames()) {
            interceptors.add(this.applicationContext.getBean(interceptorName, ServerInterceptor.class));
        }
        return ServerInterceptors.intercept(serviceDefinition, interceptors);
    }

}

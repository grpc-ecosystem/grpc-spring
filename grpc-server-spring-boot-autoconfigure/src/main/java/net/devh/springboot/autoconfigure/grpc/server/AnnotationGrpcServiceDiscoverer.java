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
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Slf4j
public class AnnotationGrpcServiceDiscoverer implements ApplicationContextAware, GrpcServiceDiscoverer {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<GrpcServiceDefinition> findGrpcServices() {
        Collection<String> beanNames = Arrays.asList(this.applicationContext.getBeanNamesForAnnotation(GrpcService.class));
        List<GrpcServiceDefinition> definitions = Lists.newArrayListWithCapacity(beanNames.size());
        GlobalServerInterceptorRegistry globalServerInterceptorRegistry = applicationContext.getBean(GlobalServerInterceptorRegistry.class);
        List<ServerInterceptor> globalInterceptorList = globalServerInterceptorRegistry.getServerInterceptors();
        for (String beanName : beanNames) {
            BindableService bindableService = this.applicationContext.getBean(beanName, BindableService.class);
            ServerServiceDefinition serviceDefinition = bindableService.bindService();
            GrpcService grpcServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, GrpcService.class);
            serviceDefinition = bindInterceptors(serviceDefinition, grpcServiceAnnotation, globalInterceptorList);
            definitions.add(new GrpcServiceDefinition(beanName, bindableService.getClass(), serviceDefinition));
            log.debug("Found gRPC service: " + serviceDefinition.getServiceDescriptor().getName() + ", bean: " + beanName + ", class: " + bindableService.getClass().getName());
        }
        return definitions;
    }

    @Override
    public Collection<GrpcCodecDefinition> findGrpcCodec() {
        Collection<String> beanNames = Arrays.asList(this.applicationContext.getBeanNamesForAnnotation(GrpcCodec.class));
        List<GrpcCodecDefinition> definitions = Lists.newArrayListWithCapacity(beanNames.size());
        for (String beanName : beanNames) {
            Codec codec = this.applicationContext.getBean(beanName, Codec.class);
            GrpcCodec annotation = applicationContext.findAnnotationOnBean(beanName, GrpcCodec.class);
            definitions.add(new GrpcCodecDefinition(codec, annotation.advertised(), annotation.codecType()));
            log.debug("Found custom gRPC custom codec: " + codec.getMessageEncoding() + ", bean: " + beanName + ", class: " + codec.getClass().getName());
        }
        return definitions;
    }

    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition, GrpcService grpcServiceAnnotation, List<ServerInterceptor> globalInterceptorList) {
        Collection<ServerInterceptor> interceptors = Lists.newArrayList();
        interceptors.addAll(globalInterceptorList);
        for (Class<? extends ServerInterceptor> serverInterceptorClass : grpcServiceAnnotation.interceptors()) {
            ServerInterceptor serverInterceptor;
            if (applicationContext.getBeanNamesForType(serverInterceptorClass).length > 0) {
                serverInterceptor = applicationContext.getBean(serverInterceptorClass);
            } else {
                try {
                    serverInterceptor = serverInterceptorClass.newInstance();
                } catch (Exception e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            interceptors.add(serverInterceptor);
        }
        return ServerInterceptors.intercept(serviceDefinition, Lists.newArrayList(interceptors));
    }
}

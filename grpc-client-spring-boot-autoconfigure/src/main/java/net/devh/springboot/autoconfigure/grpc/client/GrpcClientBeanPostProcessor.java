package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.collect.Lists;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import lombok.SneakyThrows;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class GrpcClientBeanPostProcessor implements org.springframework.beans.factory.config.BeanPostProcessor {

    private Map<String, List<Class>> beansToProcess = new HashMap<>();

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private GrpcChannelFactory channelFactory;

    @Autowired
    private GlobalClientInterceptorRegistry globalClientInterceptorRegistry;

    public GrpcClientBeanPostProcessor() {
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(GrpcClient.class)) {
                    if (!beansToProcess.containsKey(beanName)) {
                        beansToProcess.put(beanName, new ArrayList<Class>());
                    }
                    beansToProcess.get(beanName).add(clazz);
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        List<ClientInterceptor> globalInterceptorList = globalClientInterceptorRegistry.getClientInterceptors();
        if (beansToProcess.containsKey(beanName)) {
            Object target = getTargetBean(bean);
            for (Class clazz : beansToProcess.get(beanName)) {
                for (Field field : clazz.getDeclaredFields()) {
                    GrpcClient annotation = AnnotationUtils.getAnnotation(field, GrpcClient.class);
                    Channel channel;
                    if (null != annotation) {
                        if (beanFactory.containsBean(field.getName())) {
                            channel = (Channel) beanFactory.getBean(field.getName());
                        } else {
                            channel = channelFactory.createChannel(annotation.value());
                        }
                        ReflectionUtils.makeAccessible(field);
                        // bind client interceptor
                        channel = bindInterceptors(channel, annotation, globalInterceptorList);
                        ReflectionUtils.setField(field, target, channel);
                    }
                }
            }
        }
        return bean;
    }

    private Channel bindInterceptors(Channel channel, GrpcClient grpcClient, List<ClientInterceptor> globalInterceptorList) {
        Set<ClientInterceptor> interceptorSet = new HashSet<>();
        interceptorSet.addAll(globalInterceptorList);
        for (Class<? extends ClientInterceptor> clientInterceptorClass : grpcClient.interceptors()) {
            ClientInterceptor clientInterceptor;
            if (beanFactory.getBeanNamesForType(ClientInterceptor.class).length > 0) {
                clientInterceptor = beanFactory.getBean(clientInterceptorClass);
            } else {
                try {
                    clientInterceptor = clientInterceptorClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            interceptorSet.add(clientInterceptor);
        }
        return ClientInterceptors.intercept(channel, Lists.newArrayList(interceptorSet));
    }

    @SneakyThrows
    private Object getTargetBean(Object bean) {
        Object target = bean;
        while (AopUtils.isAopProxy(target)) {
            target = ((Advised) target).getTargetSource().getTarget();
        }
        return target;
    }


}

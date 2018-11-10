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

package net.devh.springboot.autoconfigure.grpc.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.stub.AbstractStub;
import lombok.SneakyThrows;

/**
 * This {@link BeanPostProcessor} searches for fields in beans that are annotated with {@link GrpcClient} and sets them.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class GrpcClientBeanPostProcessor implements BeanPostProcessor, AutoCloseable {

    private final Multimap<String, Field> beansToProcess = HashMultimap.create();

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private GrpcChannelFactory channelFactory;

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        Class<?> clazz = bean.getClass();
        do {
            for (final Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(GrpcClient.class)) {
                    ReflectionUtils.makeAccessible(field);
                    this.beansToProcess.put(beanName, field);
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (this.beansToProcess.containsKey(beanName)) {
            final Object target = getTargetBean(bean);
            for (final Field field : this.beansToProcess.get(beanName)) {
                final GrpcClient annotation = AnnotationUtils.getAnnotation(field, GrpcClient.class);

                final List<ClientInterceptor> interceptors = interceptorsFromAnnotation(annotation);
                final Channel channel = this.channelFactory.createChannel(annotation.value(), interceptors);

                final Object value = valueForField(field, channel);
                ReflectionUtils.setField(field, target, value);
            }
        }
        return bean;
    }

    /**
     * Gets or creates the {@link ClientInterceptor}s that are referenced in the given annotation.
     *
     * <p>
     * <b>Note:</b> This methods return value does not contain the global client interceptors because they are handled
     * by the {@link GrpcChannelFactory}.
     * </p>
     *
     * @param annotation The annotation to get the interceptors for.
     * @return A list containing the interceptors for the given annotation.
     * @throws BeansException If the referenced interceptors weren't found or could not be created.
     */
    protected List<ClientInterceptor> interceptorsFromAnnotation(final GrpcClient annotation) throws BeansException {
        final List<ClientInterceptor> list = Lists.newArrayList();
        for (final Class<? extends ClientInterceptor> interceptorClass : annotation.interceptors()) {
            final ClientInterceptor clientInterceptor;
            if (this.beanFactory.getBeanNamesForType(ClientInterceptor.class).length > 0) {
                clientInterceptor = this.beanFactory.getBean(interceptorClass);
            } else {
                try {
                    clientInterceptor = interceptorClass.newInstance();
                } catch (final Exception e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            list.add(clientInterceptor);
        }
        for (final String interceptorName : annotation.interceptorNames()) {
            list.add(this.beanFactory.getBean(interceptorName, ClientInterceptor.class));
        }
        return list;
    }

    /**
     * Creates the instance for the given field.
     *
     * @param field The field to create the instance for.
     * @param channel The channel that should be used to create the instance.
     * @return The value that matches the type of the given field.
     * @throws BeansException If the value of the field could not be created or the type of the field is unsupported.
     */
    protected Object valueForField(final Field field, final Channel channel) throws BeansException {
        final Class<?> fieldType = field.getType();
        if (Channel.class.equals(fieldType)) {
            return channel;
        } else if (AbstractStub.class.isAssignableFrom(fieldType)) {
            try {
                final Constructor<?> constructor = ReflectionUtils.accessibleConstructor(fieldType, Channel.class);
                return constructor.newInstance(channel);
            } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                throw new BeanInstantiationException(fieldType,
                        "Failed to create gRPC client for field: " + field, e);
            }
        } else {
            throw new InvalidPropertyException(field.getDeclaringClass(), field.getName(),
                    "Unsupported field type " + fieldType.getName());
        }
    }

    @SneakyThrows
    private Object getTargetBean(final Object bean) {
        Object target = bean;
        while (AopUtils.isAopProxy(target)) {
            target = ((Advised) target).getTargetSource().getTarget();
        }
        return target;
    }

    @Override
    public void close() {
        this.beansToProcess.clear();
    }

}

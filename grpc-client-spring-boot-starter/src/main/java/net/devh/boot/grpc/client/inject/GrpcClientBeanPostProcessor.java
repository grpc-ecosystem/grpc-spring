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

package net.devh.boot.grpc.client.inject;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Lists;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.stub.AbstractStub;
import jakarta.annotation.PostConstruct;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import net.devh.boot.grpc.client.nameresolver.NameResolverRegistration;
import net.devh.boot.grpc.client.stubfactory.FallbackStubFactory;
import net.devh.boot.grpc.client.stubfactory.StubFactory;

/**
 * This {@link BeanPostProcessor} searches for fields and methods in beans that are annotated with {@link GrpcClient}
 * and sets them.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class GrpcClientBeanPostProcessor implements BeanPostProcessor {

    private final ApplicationContext applicationContext;

    // Is only retrieved when needed to avoid too early initialization of these components,
    // which could lead to problems with the correct bean setup.
    private GrpcChannelFactory channelFactory = null;
    private List<StubTransformer> stubTransformers = null;
    private List<StubFactory> stubFactories = null;

    // For bean registration via @GrpcClientBean
    private ConfigurableListableBeanFactory configurableBeanFactory;

    /**
     * Creates a new GrpcClientBeanPostProcessor with the given ApplicationContext.
     *
     * @param applicationContext The application context that will be used to get lazy access to the
     *        {@link GrpcChannelFactory} and {@link StubTransformer}s.
     */
    public GrpcClientBeanPostProcessor(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    @PostConstruct
    public void init() {
        initGrpClientConstructorInjections();
    }

    /**
     * Triggers registering grpc client beans from GrpcClientConstructorInjection.
     */
    private void initGrpClientConstructorInjections() {
        Iterable<GrpcClientConstructorInjection.Registry> registries;
        try {
            registries = getConfigurableBeanFactory().getBean(GrpcClientConstructorInjection.class).getRegistries();
        } catch (NoSuchBeanDefinitionException ignored) {
            return;
        }

        for (GrpcClientConstructorInjection.Registry registry : registries) {
            try {
                Object clientStubInstance =
                        processInjectionPoint(null, registry.getStubClass(), registry.getClient());

                // Bind generated client stub instance to specific constructor parameter by ConstructorArgumentValues in
                // BeanDefinition
                registry.getTargetBeanDefinition()
                        .getConstructorArgumentValues()
                        .addIndexedArgumentValue(registry.getConstructorArgumentIndex(), clientStubInstance);

            } catch (final Exception e) {
                throw new BeanCreationException("@GrpcClient on class " + registry.getTargetClazz().getName(),
                        "@GrpcClient constructor injection, parameter index=" + registry.getConstructorArgumentIndex(),
                        "Unexpected exception while binding gRPC client stub instance to constructor parameter",
                        e);
            }
        }
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        do {
            processFields(clazz, bean);
            processMethods(clazz, bean);

            if (isAnnotatedWithConfiguration(clazz)) {
                processGrpcClientBeansAnnotations(clazz);
            }

            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return bean;
    }

    /**
     * Processes the bean's fields in the given class.
     *
     * @param clazz The class to process.
     * @param bean The bean to process.
     */
    private void processFields(final Class<?> clazz, final Object bean) {
        for (final Field field : clazz.getDeclaredFields()) {
            final GrpcClient annotation = AnnotationUtils.findAnnotation(field, GrpcClient.class);
            if (annotation != null) {
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, bean, processInjectionPoint(field, field.getType(), annotation));
            }
        }
    }


    /**
     * Processes the bean's methods in the given class.
     *
     * @param clazz The class to process.
     * @param bean The bean to process.
     */
    private void processMethods(final Class<?> clazz, final Object bean) {
        for (final Method method : clazz.getDeclaredMethods()) {
            final GrpcClient annotation = AnnotationUtils.findAnnotation(method, GrpcClient.class);
            if (annotation != null) {
                final Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1) {
                    throw new BeanDefinitionStoreException(
                            "Method " + method + " doesn't have exactly one parameter.");
                }
                ReflectionUtils.makeAccessible(method);
                ReflectionUtils.invokeMethod(method, bean,
                        processInjectionPoint(method, paramTypes[0], annotation));
            }
        }
    }


    /**
     * Processes the given class's {@link GrpcClientBean} annotations.
     *
     * @param clazz The class to process.
     */
    private void processGrpcClientBeansAnnotations(final Class<?> clazz) {
        for (final GrpcClientBean annotation : clazz.getAnnotationsByType(GrpcClientBean.class)) {
            final String beanNameToCreate = getBeanName(annotation);
            try {
                final ConfigurableListableBeanFactory beanFactory = getConfigurableBeanFactory();
                final Object beanValue =
                        processInjectionPoint(null, annotation.clazz(), annotation.client());
                beanFactory.registerSingleton(beanNameToCreate, beanValue);
            } catch (final Exception e) {
                throw new BeanCreationException(annotation + " on class " + clazz.getName(), beanNameToCreate,
                        "Unexpected exception while creating and registering bean",
                        e);
            }
        }
    }

    /**
     * Processes the given injection point and computes the appropriate value for the injection.
     *
     * @param <T> The type of the value to be injected.
     * @param injectionTarget The target of the injection.
     * @param injectionType The class that will be used to compute injection.
     * @param annotation The annotation on the target with the metadata for the injection.
     * @return The value to be injected for the given injection point.
     */
    protected <T> T processInjectionPoint(final Member injectionTarget, final Class<T> injectionType,
            final GrpcClient annotation) {
        final List<ClientInterceptor> interceptors = interceptorsFromAnnotation(annotation);
        final String name = annotation.value();
        final Channel channel;
        try {
            channel = getChannelFactory().createChannel(name, interceptors, annotation.sortInterceptors());
            if (channel == null) {
                throw new IllegalStateException("Channel factory created a null channel for " + name);
            }
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to create channel: " + name, e);
        }

        final T value = valueForMember(name, injectionTarget, injectionType, channel);
        if (value == null) {
            throw new IllegalStateException(
                    "Injection value is null unexpectedly for " + name + " at " + injectionTarget);
        }

        return value;
    }

    /**
     * Lazy getter for the {@link GrpcChannelFactory}.
     *
     * @return The grpc channel factory to use.
     */
    private GrpcChannelFactory getChannelFactory() {
        if (this.channelFactory == null) {
            // Ensure that the NameResolverProviders have been registered
            this.applicationContext.getBean(NameResolverRegistration.class);
            final GrpcChannelFactory factory = this.applicationContext.getBean(GrpcChannelFactory.class);
            this.channelFactory = factory;
            return factory;
        }
        return this.channelFactory;
    }

    /**
     * Lazy getter for the {@link StubTransformer}s.
     *
     * @return The stub transformers to use.
     */
    private List<StubTransformer> getStubTransformers() {
        if (this.stubTransformers == null) {
            final Collection<StubTransformer> transformers =
                    this.applicationContext.getBeansOfType(StubTransformer.class).values();
            this.stubTransformers = new ArrayList<>(transformers);
            return this.stubTransformers;
        }
        return this.stubTransformers;
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
            if (this.applicationContext.getBeanNamesForType(interceptorClass).length > 0) {
                clientInterceptor = this.applicationContext.getBean(interceptorClass);
            } else {
                try {
                    clientInterceptor = interceptorClass.getConstructor().newInstance();
                } catch (final Exception e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            list.add(clientInterceptor);
        }
        for (final String interceptorName : annotation.interceptorNames()) {
            list.add(this.applicationContext.getBean(interceptorName, ClientInterceptor.class));
        }
        return list;
    }

    /**
     * Creates the instance to be injected for the given member.
     *
     * @param <T> The type of the instance to be injected.
     * @param name The name that was used to create the channel.
     * @param injectionTarget The target member for the injection.
     * @param injectionType The class that should injected.
     * @param channel The channel that should be used to create the instance.
     * @return The value that matches the type of the given field.
     * @throws BeansException If the value of the field could not be created or the type of the field is unsupported.
     */
    protected <T> T valueForMember(final String name, final Member injectionTarget,
            final Class<T> injectionType,
            final Channel channel) throws BeansException {
        if (Channel.class.equals(injectionType)) {
            return injectionType.cast(channel);
        } else if (AbstractStub.class.isAssignableFrom(injectionType)) {

            @SuppressWarnings("unchecked") // Eclipse incorrectly marks this as not required
            AbstractStub<?> stub = createStub(
                    (Class<? extends AbstractStub<?>>) injectionType.asSubclass(AbstractStub.class), channel);
            for (final StubTransformer stubTransformer : getStubTransformers()) {
                stub = stubTransformer.transform(name, stub);
            }
            return injectionType.cast(stub);
        } else {
            if (injectionTarget != null) {
                throw new InvalidPropertyException(injectionTarget.getDeclaringClass(), injectionTarget.getName(),
                        "Unsupported type " + injectionType.getName());
            } else {
                throw new BeanInstantiationException(injectionType, "Unsupported grpc stub or channel type");
            }
        }
    }

    /**
     * Creates a stub instance for the specified stub type using the resolved {@link StubFactory}.
     *
     * @param stubClass The stub class that needs to be created.
     * @param channel The gRPC channel associated with the created stub, passed as a parameter to the stub factory.
     * @return A newly created gRPC stub.
     * @throws BeanInstantiationException If the stub couldn't be created, either because the type isn't supported or
     *         because of a failure in creation.
     */
    private AbstractStub<?> createStub(final Class<? extends AbstractStub<?>> stubClass, final Channel channel) {
        final StubFactory factory = getStubFactories().stream()
                .filter(stubFactory -> stubFactory.isApplicable(stubClass))
                .findFirst()
                .orElseThrow(() -> new BeanInstantiationException(stubClass,
                        "Unsupported stub type: " + stubClass.getName() + " -> Please report this issue."));

        try {
            return factory.createStub(stubClass, channel);
        } catch (final Exception exception) {
            throw new BeanInstantiationException(stubClass, "Failed to create gRPC stub of type " + stubClass.getName(),
                    exception);
        }
    }

    /**
     * Lazy getter for the list of defined {@link StubFactory} beans.
     *
     * @return A list of all defined {@link StubFactory} beans.
     */
    private List<StubFactory> getStubFactories() {
        if (this.stubFactories == null) {
            this.stubFactories = new ArrayList<>(this.applicationContext.getBeansOfType(StubFactory.class).values());
            this.stubFactories.add(new FallbackStubFactory());
        }
        return this.stubFactories;
    }

    /**
     * Lazy factory getter from the context for bean registration with {@link GrpcClientBean} annotations.
     *
     * @return configurable bean factory
     */
    private ConfigurableListableBeanFactory getConfigurableBeanFactory() {
        if (this.configurableBeanFactory == null) {
            this.configurableBeanFactory = ((ConfigurableApplicationContext) this.applicationContext).getBeanFactory();
        }
        return this.configurableBeanFactory;
    }

    /**
     * Gets the bean name from the given annotation.
     *
     * @param grpcClientBean The annotation to extract it from.
     * @return The extracted name.
     */
    private String getBeanName(final GrpcClientBean grpcClientBean) {
        if (!grpcClientBean.beanName().isEmpty()) {
            return grpcClientBean.beanName();
        } else {
            return grpcClientBean.client().value() + grpcClientBean.clazz().getSimpleName();
        }
    }

    /**
     * Checks whether the given class is annotated with {@link Configuration}.
     *
     * @param clazz The class to check.
     * @return True, if the given class is annotated with {@link Configuration}. False otherwise.
     */
    private boolean isAnnotatedWithConfiguration(final Class<?> clazz) {
        final Configuration configurationAnnotation = AnnotationUtils.findAnnotation(clazz, Configuration.class);
        return configurationAnnotation != null;
    }

}

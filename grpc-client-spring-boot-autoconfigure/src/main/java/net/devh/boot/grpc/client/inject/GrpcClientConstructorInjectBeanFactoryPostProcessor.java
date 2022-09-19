package net.devh.boot.grpc.client.inject;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

public class GrpcClientConstructorInjectBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

        GrpcClientConstructorInjection grpcClientConstructorInjection = new GrpcClientConstructorInjection();

        // Use bean name to get bean class to avoid triggering bean init
        beanFactory.getBeanNamesIterator().forEachRemaining(beanName -> {
            Class<?> clazz = beanFactory.getType(beanName);
            if (clazz == null) {
                return;
            }

            // Search for GrpcClient annotation in all parameters of all constructors
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                for (Parameter parameter : constructor.getParameters()) {
                    GrpcClient client = parameter.getAnnotation(GrpcClient.class);
                    if (client == null) {
                        continue;
                    }
                    GrpcClientConstructorInjection.GrpcClientBeanInjection injection =
                            new GrpcClientConstructorInjection.GrpcClientBeanInjection(parameter.getType(), client, clazz);
                    grpcClientConstructorInjection.add(injection);
                }
            }
        });

        beanFactory.registerSingleton("grpcClientInjects", grpcClientConstructorInjection);
    }
}

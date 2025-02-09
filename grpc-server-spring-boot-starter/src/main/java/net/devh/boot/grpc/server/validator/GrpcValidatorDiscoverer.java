/*
 * Copyright (c) 2016-2025 The gRPC-Spring Authors
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

package net.devh.boot.grpc.server.validator;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.netflix.discovery.shared.Pair;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The {@code GrpcValidatorDiscoverer} class is responsible for discovering and registering gRPC validators
 * in the Spring application context based on custom annotations.
 *
 * @author Pritesh (priteshdpawar53@gmail.com)
 * @since 02/10/25
 *
 * <p>
 * Example usage: If a service contains a method annotated with {@link GrpcValidator} for a specific request class,
 * the {@code GrpcValidatorDiscoverer} will discover this method and register it in the map so that it can later be
 * used for validation during gRPC request processing.
 * </p>
 */
@Slf4j
@Component
public class GrpcValidatorDiscoverer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * A map of request classes to their corresponding validator bean and method.
     * The map is populated during the initialization phase using the {@link GrpcValidator} annotation.
     * The key is the request class, and the value is a {@link Pair} containing the validator bean and method name.
     */
    @Getter
    private Map<Class<?>, Pair<Object, String>> requestValidatorMethodMap;

    /**
     * Sets the application context. This method is called by the Spring container.
     * It allows access to the Spring {@link ApplicationContext} to retrieve beans and methods.
     *
     * @param applicationContext the Spring {@link ApplicationContext} to be set
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes the {@code GrpcValidatorDiscoverer} by scanning all beans in the Spring context that are
     * annotated with {@link Service}. For each of these beans, the method scans its declared methods for the
     * {@link GrpcValidator} annotation.
     * <p>
     * It collects the mapping between request classes and their corresponding validator beans and methods,
     * storing it in the {@link #requestValidatorMethodMap} field.
     * </p>
     * <p>
     * This method is executed after the bean has been initialized (as indicated by the {@link PostConstruct} annotation),
     * and it logs the initialization status and the resulting map of validators.
     * </p>
     */
    @PostConstruct
    public void init() {
        log.info("Initializing registration of GrpcValidator annotation");

        // Discover beans annotated with @Service and their methods annotated with @GrpcValidator
        requestValidatorMethodMap = applicationContext.getBeansWithAnnotation(Service.class).values().stream()
                .flatMap(bean -> Arrays.stream(bean.getClass().getDeclaredMethods())) // Flatten the methods in the bean
                .filter(method -> method.isAnnotationPresent(GrpcValidator.class)) // Filter methods annotated with @GrpcValidator
                .map(method -> {
                    GrpcValidator validator = method.getAnnotation(GrpcValidator.class);
                    Object validatorBean = applicationContext.getBean(validator.validatorClass()); // Retrieve the validator bean from the context
                    return new Pair<>(validator.requestClass(), new Pair<>(validatorBean, validator.validatorMethod())); // Return a pair of the request class and the validator info
                })
                .collect(Collectors.toMap(Pair::first, Pair::second)); // Collect into a map: request class -> (validator bean, method name)

        log.info("Request to validator map instantiated: {}", requestValidatorMethodMap);
    }
}

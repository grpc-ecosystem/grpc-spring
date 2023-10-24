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

package net.devh.boot.grpc.server.interceptor;

import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.common.util.InterceptorOrder.beanFactoryAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableList;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import net.devh.boot.grpc.common.util.InterceptorOrder;

/**
 * The global server interceptor registry keeps references to all {@link ServerInterceptor}s that should be registered
 * to all server channels. The interceptors will be applied in the same order they as specified by the
 * {@link #sortInterceptors(List)} method.
 *
 * <p>
 * <b>Note:</b> Custom interceptors will be appended to the global interceptors and applied using
 * {@link ServerInterceptors#interceptForward(BindableService, ServerInterceptor...)}.
 * </p>
 *
 * @author Michael (yidongnan@gmail.com)
 */
public class GlobalServerInterceptorRegistry {

    private final ApplicationContext applicationContext;

    private ImmutableList<ServerInterceptor> sortedServerInterceptors;

    /**
     * Creates a new GlobalServerInterceptorRegistry.
     *
     * @param applicationContext The application context to fetch the {@link GlobalServerInterceptorConfigurer} beans
     *        from.
     */
    public GlobalServerInterceptorRegistry(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    /**
     * Gets the immutable list of global server interceptors.
     *
     * @return The list of globally registered server interceptors.
     */
    public ImmutableList<ServerInterceptor> getServerInterceptors() {
        if (this.sortedServerInterceptors == null) {
            this.sortedServerInterceptors = ImmutableList.copyOf(initServerInterceptors());
        }
        return this.sortedServerInterceptors;
    }

    /**
     * Initializes the list of server interceptors.
     *
     * @return The list of global server interceptors.
     */
    protected List<ServerInterceptor> initServerInterceptors() {
        final List<ServerInterceptor> interceptors = new ArrayList<>();
        for (final GlobalServerInterceptorConfigurer configurer : this.applicationContext
                .getBeansOfType(GlobalServerInterceptorConfigurer.class).values()) {
            configurer.configureServerInterceptors(interceptors);
        }
        sortInterceptors(interceptors);
        return interceptors;
    }

    /**
     * Sorts the given list of interceptors. Use this method if you want to sort custom interceptors. The default
     * implementation will sort them by using a
     * {@link InterceptorOrder#beanFactoryAwareOrderComparator(ApplicationContext, Class)
     * beanDefinitionAwareOrderComparator}.
     *
     * @param interceptors The interceptors to sort.
     */
    public void sortInterceptors(final List<? extends ServerInterceptor> interceptors) {
        interceptors.sort(beanFactoryAwareOrderComparator(this.applicationContext, ServerInterceptor.class));
    }

}

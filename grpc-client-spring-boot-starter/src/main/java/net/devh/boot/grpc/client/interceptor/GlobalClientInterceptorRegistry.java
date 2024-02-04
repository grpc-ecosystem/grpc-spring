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

package net.devh.boot.grpc.client.interceptor;

import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.common.util.InterceptorOrder.beanFactoryAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.google.common.collect.ImmutableList;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;

/**
 * The global client interceptor registry keeps references to all {@link ClientInterceptor}s that should be registered
 * to all client channels. The interceptors will be applied in the same order they as specified by the
 * {@link #sortInterceptors(List)} method.
 *
 * <p>
 * <b>Note:</b> Custom interceptors will be appended to the global interceptors and applied using
 * {@link ClientInterceptors#interceptForward(Channel, ClientInterceptor...)}.
 * </p>
 *
 * @author Michael (yidongnan@gmail.com)
 */
public class GlobalClientInterceptorRegistry {

    private final ApplicationContext applicationContext;

    private ImmutableList<ClientInterceptor> sortedClientInterceptors;

    /**
     * Creates a new GlobalClientInterceptorRegistry.
     *
     * @param applicationContext The application context to fetch the {@link GlobalClientInterceptorConfigurer} beans
     *        from.
     */
    public GlobalClientInterceptorRegistry(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    /**
     * Gets the immutable list of global server interceptors.
     *
     * @return The list of globally registered server interceptors.
     */
    public ImmutableList<ClientInterceptor> getClientInterceptors() {
        if (this.sortedClientInterceptors == null) {
            this.sortedClientInterceptors = ImmutableList.copyOf(initClientInterceptors());
        }
        return this.sortedClientInterceptors;
    }

    /**
     * Initializes the list of client interceptors.
     *
     * @return The list of global client interceptors.
     */
    protected List<ClientInterceptor> initClientInterceptors() {
        final List<ClientInterceptor> interceptors = new ArrayList<>();
        for (final GlobalClientInterceptorConfigurer configurer : this.applicationContext
                .getBeansOfType(GlobalClientInterceptorConfigurer.class).values()) {
            configurer.configureClientInterceptors(interceptors);
        }
        sortInterceptors(interceptors);
        return interceptors;
    }

    /**
     * Sorts the given list of interceptors. Use this method if you want to sort custom interceptors. The default
     * implementation will sort them by using then {@link AnnotationAwareOrderComparator}.
     *
     * @param interceptors The interceptors to sort.
     */
    public void sortInterceptors(final List<? extends ClientInterceptor> interceptors) {
        interceptors.sort(beanFactoryAwareOrderComparator(this.applicationContext, ClientInterceptor.class));
    }

}

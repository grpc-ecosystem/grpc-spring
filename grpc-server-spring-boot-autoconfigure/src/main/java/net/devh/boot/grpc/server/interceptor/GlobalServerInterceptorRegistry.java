/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.interceptor;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.google.common.collect.ImmutableList;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;

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
     * implementation will sort them by using then {@link AnnotationAwareOrderComparator}.
     *
     * @param interceptors The interceptors to sort.
     */
    public void sortInterceptors(final List<? extends ServerInterceptor> interceptors) {
        interceptors.sort(AnnotationAwareOrderComparator.INSTANCE);
    }

}

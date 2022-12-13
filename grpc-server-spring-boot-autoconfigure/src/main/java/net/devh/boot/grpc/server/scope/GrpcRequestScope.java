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

package net.devh.boot.grpc.server.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import com.google.common.util.concurrent.MoreExecutors;

import io.grpc.Context;
import io.grpc.Context.CancellationListener;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * The scope for beans that have their lifecycle bound to the grpc {@link Context}.
 *
 * <p>
 * <b>Note:</b> If you write the {@link Bean @Bean} definition of this class, you must use the {@code static} keyword.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@GrpcGlobalServerInterceptor
@Order(InterceptorOrder.ORDER_FIRST)
public class GrpcRequestScope implements Scope, BeanFactoryPostProcessor, ServerInterceptor, CancellationListener {

    public static final String GRPC_REQUEST_SCOPE_NAME = "grpcRequest";
    private static final String GRPC_REQUEST_SCOPE_ID = "grpc-request";
    private static final Context.Key<ScopedBeansContainer> GRPC_REQUEST_KEY = Context.key("grpcRequestScope");

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
        factory.registerScope(GRPC_REQUEST_SCOPE_NAME, this);
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        ScopedBeansContainer container = new ScopedBeansContainer();
        Context context = Context.current().withValue(GRPC_REQUEST_KEY, container);
        context.addListener(this, MoreExecutors.directExecutor());
        return Contexts.interceptCall(context, call, headers, next);
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return getCurrentScopeContainer().getOrCreate(name, objectFactory);
    }

    @Override
    public Object remove(String name) {
        return getCurrentScopeContainer().remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        getCurrentScopeContainer().registerDestructionCallback(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return GRPC_REQUEST_SCOPE_ID;
    }

    @Override
    public void cancelled(Context context) {
        final ScopedBeansContainer container = GRPC_REQUEST_KEY.get(context);
        if (container != null) {
            container.destroy();
        }
    }

    /**
     * Gets the current container for the grpc request scope.
     *
     * @return The currently active scope container.
     * @throws IllegalStateException If the grpc request scope is currently not active.
     */
    private ScopedBeansContainer getCurrentScopeContainer() {
        ScopedBeansContainer scopedBeansContainer = GRPC_REQUEST_KEY.get();
        if (scopedBeansContainer == null) {
            throw new IllegalStateException(
                    "Trying to access grpcRequest-Scope, but it was not assigned to this execution context.\n"
                            + "There is either no active grpc request or you didn't transfer the correct GrpcContext to this async execution context.");
        }
        return scopedBeansContainer;
    }

    /**
     * Container for all beans used in the active scope.
     */
    private static class ScopedBeansContainer {

        private final Map<String, ScopedBeanReference> references = new ConcurrentHashMap<>();

        /**
         * Gets or creates the bean with the given name using the given object factory.
         *
         * @param name The name of the bean.
         * @param objectFactory The object factory used to create new instances.
         * @return The bean associated with the given name.
         */
        public Object getOrCreate(final String name, final ObjectFactory<?> objectFactory) {
            return this.references.computeIfAbsent(name, key -> new ScopedBeanReference(objectFactory))
                    .getBean();
        }

        /**
         * Removes the bean with the given name from this scope.
         *
         * @param name The name of the bean to remove.
         * @return The bean instances that was removed from the scope or null, if it wasn't present.
         */
        public Object remove(final String name) {
            final ScopedBeanReference ref = this.references.remove(name);
            if (ref == null) {
                return null;
            } else {
                return ref.getBeanIfExists();
            }
        }

        /**
         * Attaches a destruction callback to the bean with the given name.
         *
         * @param name The name of the bean to attach the destruction callback to.
         * @param callback The callback to register for the bean.
         */
        public void registerDestructionCallback(final String name, final Runnable callback) {
            final ScopedBeanReference ref = this.references.get(name);
            if (ref != null) {
                ref.setDestructionCallback(callback);
            }
        }

        /**
         * Destroys all beans in the scope and executes their destruction callbacks.
         */
        public void destroy() {
            final List<RuntimeException> errors = new ArrayList<>();
            final Iterator<ScopedBeanReference> it = this.references.values().iterator();
            while (it.hasNext()) {
                ScopedBeanReference val = it.next();
                it.remove();
                try {
                    val.destroy();
                } catch (RuntimeException e) {
                    errors.add(e);
                }
            }
            if (!errors.isEmpty()) {
                RuntimeException rex = errors.remove(0);
                for (RuntimeException error : errors) {
                    rex.addSuppressed(error);
                }
                throw rex;
            }
        }

    }

    /**
     * Container for a single scoped bean. This class manages the bean creation
     */
    private static class ScopedBeanReference {

        private final ObjectFactory<?> objectFactory;
        private Object bean;
        private Runnable destructionCallback;

        /**
         * Creates a new scoped bean reference using the given object factory.
         *
         * @param objectFactory The object factory used to create instances of that bean.
         */
        public ScopedBeanReference(ObjectFactory<?> objectFactory) {
            this.objectFactory = objectFactory;
        }

        /**
         * Gets or creates the bean managed by this instance.
         *
         * @return The existing or newly created bean instance.
         */
        public synchronized Object getBean() {
            if (this.bean == null) {
                this.bean = this.objectFactory.getObject();
            }
            return this.bean;
        }

        /**
         * Gets the bean managed by this instance, if it exists.
         *
         * @return The existing bean or null.
         */
        public Object getBeanIfExists() {
            return this.bean;
        }

        /**
         * Sets the given callback used to destroy the managed bean.
         *
         * @param destructionCallback The destruction callback to use.
         */
        public void setDestructionCallback(final Runnable destructionCallback) {
            this.destructionCallback = destructionCallback;
        }

        /**
         * Executes the destruction callback if set and clears the internal bean references.
         */
        public synchronized void destroy() {
            Runnable callback = this.destructionCallback;
            if (callback != null) {
                callback.run();
            }
            this.bean = null;
            this.destructionCallback = null;
        }

        @Override
        public String toString() {
            return "ScopedBeanReference [objectFactory=" + this.objectFactory + ", bean=" + this.bean + "]";
        }

    }

}

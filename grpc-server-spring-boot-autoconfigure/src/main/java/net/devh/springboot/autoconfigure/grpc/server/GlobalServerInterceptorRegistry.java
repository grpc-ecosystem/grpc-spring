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

package net.devh.springboot.autoconfigure.grpc.server;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;

import io.grpc.ServerInterceptor;
import lombok.Getter;

/**
 * The global server interceptor registry keeps references to all {@link ServerInterceptor}s that should be registered
 * globally. The interceptors will be applied in the same order they are added to this registry.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class GlobalServerInterceptorRegistry implements ApplicationContextAware {

    @Getter
    private final List<ServerInterceptor> serverInterceptors = Lists.newArrayList();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        final Map<String, GlobalServerInterceptorConfigurer> map =
                this.applicationContext.getBeansOfType(GlobalServerInterceptorConfigurer.class);
        for (final GlobalServerInterceptorConfigurer globalServerInterceptorConfigurerAdapter : map.values()) {
            globalServerInterceptorConfigurerAdapter.addServerInterceptors(this);
        }
    }

    /**
     * Adds the given {@link ServerInterceptor} to the list of globally registered interceptors.
     *
     * @param interceptor The interceptor to add.
     * @return This instance for chaining.
     */
    public GlobalServerInterceptorRegistry addServerInterceptors(final ServerInterceptor interceptor) {
        this.serverInterceptors.add(interceptor);
        return this;
    }

}

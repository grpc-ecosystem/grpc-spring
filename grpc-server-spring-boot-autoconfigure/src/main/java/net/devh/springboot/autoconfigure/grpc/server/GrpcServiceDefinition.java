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

import io.grpc.ServerServiceDefinition;

/**
 * Container class that contains all relevant information about a grpc service.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 * @see GrpcServiceDiscoverer
 */
public class GrpcServiceDefinition {

    private final String beanName;
    private final Class<?> beanClazz;
    private final ServerServiceDefinition definition;

    /**
     * Creates a new GrpcServiceDefinition.
     *
     * @param beanName The name of the grpc service bean in the spring context.
     * @param beanClazz The class of the grpc service bean.
     * @param definition The grpc service definition.
     */
    public GrpcServiceDefinition(final String beanName, final Class<?> beanClazz,
            final ServerServiceDefinition definition) {
        this.beanName = beanName;
        this.beanClazz = beanClazz;
        this.definition = definition;
    }

    /**
     * Gets the name of the grpc service bean.
     *
     * @return The name of the bean.
     */
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * Gets the class of the grpc service bean.
     *
     * @return The class of the grpc service bean.
     */
    public Class<?> getBeanClazz() {
        return this.beanClazz;
    }

    /**
     * Gets the grpc service definition.
     *
     * @return The grpc service definition.
     */
    public ServerServiceDefinition getDefinition() {
        return this.definition;
    }

}

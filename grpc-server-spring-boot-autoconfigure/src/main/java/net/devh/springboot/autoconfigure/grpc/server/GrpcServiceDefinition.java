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
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class GrpcServiceDefinition {
    private final String beanName;
    private final Class<?> beanClazz;
    private final ServerServiceDefinition definition;

    public GrpcServiceDefinition(String beanName, Class<?> beanClazz, ServerServiceDefinition definition) {
        super();
        this.beanName = beanName;
        this.beanClazz = beanClazz;
        this.definition = definition;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public Class<?> getBeanClazz() {
        return this.beanClazz;
    }

    public ServerServiceDefinition getDefinition() {
        return this.definition;
    }
}

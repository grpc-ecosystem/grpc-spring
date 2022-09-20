/*
 * Copyright (c) 2016-2022 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.client.inject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

class GrpcClientConstructorInjection {

    private final ArrayList<GrpcClientBeanInjection> injections = new ArrayList<>();

    @SuppressWarnings("ClassExplicitlyAnnotation")
    static class GrpcClientBeanInjection implements GrpcClientBean {

        private final Class<?> stubClazz;
        private final Class<?> targetClazz;
        private final GrpcClient client;

        public GrpcClientBeanInjection(Class<?> stubClazz, GrpcClient client, Class<?> targetClazz) {
            this.stubClazz = stubClazz;
            this.client = client;
            this.targetClazz = targetClazz;
        }

        @Override
        public Class<?> clazz() {
            return stubClazz;
        }

        @Override
        public String beanName() {
            return client.beanName();
        }

        @Override
        public GrpcClient client() {
            return client;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return GrpcClientBean.class;
        }

        public Class<?> getTargetClazz() {
            return targetClazz;
        }
    }

    public ArrayList<GrpcClientBeanInjection> getInjections() {
        return injections;
    }

    public GrpcClientConstructorInjection add(GrpcClientBeanInjection injection) {
        injections.add(injection);
        return this;
    }
}

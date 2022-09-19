package net.devh.boot.grpc.client.inject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;

class GrpcClientConstructorInjection implements Iterable<GrpcClientConstructorInjection.GrpcClientBeanInjection> {

    private final ArrayList<GrpcClientBeanInjection> injections = new ArrayList<>();

    @SuppressWarnings("ClassExplicitlyAnnotation")
    static class GrpcClientBeanInjection implements GrpcClientBean {

        private final Class<?> stubClazz;
        private final Class<?> injectClazz;
        private final GrpcClient client;

        public GrpcClientBeanInjection(Class<?> stubClazz, GrpcClient client, Class<?> injectClazz) {
            this.stubClazz = stubClazz;
            this.client = client;
            this.injectClazz = injectClazz;
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

        public Class<?> getInjectClazz() {
            return injectClazz;
        }

    }

    @Override
    public Iterator<GrpcClientBeanInjection> iterator() {
        return injections.iterator();
    }

    public GrpcClientConstructorInjection add(GrpcClientBeanInjection injection) {
        injections.add(injection);
        return this;
    }
}

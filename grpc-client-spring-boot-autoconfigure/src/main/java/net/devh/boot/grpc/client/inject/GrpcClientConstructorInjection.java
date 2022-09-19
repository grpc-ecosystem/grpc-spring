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

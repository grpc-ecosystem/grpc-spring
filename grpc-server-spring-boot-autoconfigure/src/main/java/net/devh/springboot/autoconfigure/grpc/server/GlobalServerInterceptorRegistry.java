package net.devh.springboot.autoconfigure.grpc.server;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;

import io.grpc.ServerInterceptor;

public class GlobalServerInterceptorRegistry implements ApplicationContextAware {

    private final List<ServerInterceptor> serverInterceptors = Lists.newArrayList();
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, GlobalServerInterceptorConfigurerAdapter> map = applicationContext.getBeansOfType(GlobalServerInterceptorConfigurerAdapter.class);
        for (GlobalServerInterceptorConfigurerAdapter globalServerInterceptorConfigurerAdapter : map.values()) {
            globalServerInterceptorConfigurerAdapter.addServerInterceptors(this);
        }
    }

    public GlobalServerInterceptorRegistry addServerInterceptors(ServerInterceptor interceptor) {
        serverInterceptors.add(interceptor);
        return this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public List<ServerInterceptor> getServerInterceptors() {
        return serverInterceptors;
    }
}
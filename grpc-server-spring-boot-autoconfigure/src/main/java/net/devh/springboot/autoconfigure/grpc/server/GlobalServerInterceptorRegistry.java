package net.devh.springboot.autoconfigure.grpc.server;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;

import io.grpc.ServerInterceptor;
import lombok.Getter;

public class GlobalServerInterceptorRegistry implements ApplicationContextAware {

    @Getter
    private final List<ServerInterceptor> serverInterceptors = Lists.newArrayList();
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, GlobalServerInterceptorConfigurer> map = applicationContext.getBeansOfType(GlobalServerInterceptorConfigurer.class);
        for (GlobalServerInterceptorConfigurer globalServerInterceptorConfigurerAdapter : map.values()) {
            globalServerInterceptorConfigurerAdapter.addServerInterceptors(this);
        }
    }

    public GlobalServerInterceptorRegistry addServerInterceptors(ServerInterceptor interceptor) {
        serverInterceptors.add(interceptor);
        return this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}

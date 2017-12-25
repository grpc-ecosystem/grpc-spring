package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.collect.Lists;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import io.grpc.ClientInterceptor;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class GlobalClientInterceptorRegistry implements ApplicationContextAware {

    private final List<ClientInterceptor> clientInterceptors = Lists.newArrayList();
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, GlobalClientInterceptorConfigurerAdapter> map = applicationContext.getBeansOfType(GlobalClientInterceptorConfigurerAdapter.class);
        for (GlobalClientInterceptorConfigurerAdapter globalClientInterceptorConfigurerAdapter : map.values()) {
            globalClientInterceptorConfigurerAdapter.addClientInterceptors(this);
        }
    }

    public GlobalClientInterceptorRegistry addClientInterceptors(ClientInterceptor interceptor) {
        clientInterceptors.add(interceptor);
        return this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public List<ClientInterceptor> getClientInterceptors() {
        return clientInterceptors;
    }
}
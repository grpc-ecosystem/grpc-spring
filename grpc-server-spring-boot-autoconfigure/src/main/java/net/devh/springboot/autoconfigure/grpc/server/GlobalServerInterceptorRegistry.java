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
 * The global server interceptor registry keeps references to all {@link ServerInterceptor}s that
 * should be registered globally. The interceptors will be applied in the same order they are added
 * to this registry.
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

    public GlobalServerInterceptorRegistry addServerInterceptors(final ServerInterceptor interceptor) {
        this.serverInterceptors.add(interceptor);
        return this;
    }

}

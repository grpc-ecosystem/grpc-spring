package net.devh.springboot.autoconfigure.grpc.client;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import lombok.Getter;

/**
 * The global client interceptor registry keeps references to all {@link ClientInterceptor}s that
 * should be registered globally. The interceptors will be applied in the same order they are added
 * to this registry.
 *
 * <p>
 * <b>Note:</b> The ClientInterceptors that were applied last to a {@link Channel} will be called
 * first.
 * </p>
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class GlobalClientInterceptorRegistry implements ApplicationContextAware {

    @Getter
    private final List<ClientInterceptor> clientInterceptors = Lists.newArrayList();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        final Map<String, GlobalClientInterceptorConfigurer> map =
                this.applicationContext.getBeansOfType(GlobalClientInterceptorConfigurer.class);
        for (final GlobalClientInterceptorConfigurer globalClientInterceptorConfigurerAdapter : map.values()) {
            globalClientInterceptorConfigurerAdapter.addClientInterceptors(this);
        }
    }

    public GlobalClientInterceptorRegistry addClientInterceptors(final ClientInterceptor interceptor) {
        this.clientInterceptors.add(interceptor);
        return this;
    }

}

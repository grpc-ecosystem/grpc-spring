package my.suveng.cloud_order_demo.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步线程池配置
 * 详细查看 {@link org.springframework.scheduling.annotation.Async} 原理
 * @author suwenguang
 **/
@Configuration
@EnableAsync
public class AsyncThreadPoolConfig extends AsyncConfigurerSupport {
	@Autowired
	private BeanFactory beanFactory;

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(7);
		threadPoolTaskExecutor.setMaxPoolSize(42);
		threadPoolTaskExecutor.setQueueCapacity(11);
		threadPoolTaskExecutor.setThreadNamePrefix("async-");
		threadPoolTaskExecutor.initialize();

		return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
	}
}

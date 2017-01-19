package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.internal.LogExceptionRunnable;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Slf4j
public class AddressChannelNameResolver extends NameResolver {

    private final String name;
    private final GrpcChannelProperties properties;
    private final Attributes attributes;

    private final SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;
    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private ScheduledExecutorService timerService;
    @GuardedBy("this")
    private ExecutorService executor;
    @GuardedBy("this")
    private ScheduledFuture<?> resolutionTask;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;

    public AddressChannelNameResolver(String name, GrpcChannelProperties properties, Attributes attributes, SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource,
                                      SharedResourceHolder.Resource<ExecutorService> executorResource) {
        this.name = name;
        this.properties = properties;
        this.attributes = attributes;
        this.timerServiceResource = timerServiceResource;
        this.executorResource = executorResource;
    }

    @Override
    public String getServiceAuthority() {
        return name;
    }

    @Override
    public final synchronized void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "already started");
        timerService = SharedResourceHolder.get(timerServiceResource);
        this.listener = listener;
        executor = SharedResourceHolder.get(executorResource);
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }

    @SuppressWarnings("unchecked")
    private void replace(List list, int max, Object defaultValue) {
        list.replaceAll(o -> {
            if (o == null) {
                return defaultValue;
            } else {
                return o;
            }
        });
        for (int i = list.size(); i < max; i++) {
            list.add(defaultValue);
        }
    }

    @Override
    public final synchronized void refresh() {
        Preconditions.checkState(listener != null, "not started");
        resolve();
    }

    private final Runnable resolutionRunnable = new Runnable() {
        @Override
        public void run() {
            Listener savedListener;
            synchronized (AddressChannelNameResolver.this) {
                // If this task is started by refresh(), there might already be a scheduled task.
                if (resolutionTask != null) {
                    resolutionTask.cancel(false);
                    resolutionTask = null;
                }
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }
            try {
                int max = Math.max(properties.getHost().size(), properties.getPort().size());
                replace(properties.getHost(), max, GrpcChannelProperties.DEFAULT_HOST);
                replace(properties.getPort(), max, GrpcChannelProperties.DEFAULT_PORT);

                if (properties.getHost().size() != properties.getPort().size()) {
                    log.error("config grpc server {} error, hosts length isn't equals ports length,hosts [{}], ports [{}]", properties.getHost(), properties.getPort());
                    synchronized (AddressChannelNameResolver.this) {
                        if (shutdown) {
                            return;
                        }
                        // Because timerService is the single-threaded GrpcUtil.TIMER_SERVICE in production,
                        // we need to delegate the blocking work to the executor
                        resolutionTask = timerService.schedule(new LogExceptionRunnable(resolutionRunnableOnExecutor), 1, TimeUnit.MINUTES);
                    }
                    savedListener.onError(Status.UNAVAILABLE.withCause(new RuntimeException("grpc config error")));
                    return;
                }

                List<List<ResolvedServerInfo>> serversList = Lists.newArrayList();
                for (int i = 0; i < properties.getHost().size(); i++) {
                    List<ResolvedServerInfo> servers = new ArrayList<>();
                    servers.add(new ResolvedServerInfo(InetSocketAddress.createUnresolved(properties.getHost().get(i), properties.getPort().get(i)), Attributes.EMPTY));
                    serversList.add(servers);
                }
                savedListener.onUpdate(serversList, Attributes.EMPTY);
            } finally {
                synchronized (AddressChannelNameResolver.this) {
                    resolving = false;
                }
            }
        }
    };

    private final Runnable resolutionRunnableOnExecutor = new Runnable() {
        @Override
        public void run() {
            synchronized (AddressChannelNameResolver.this) {
                if (!shutdown) {
                    executor.execute(resolutionRunnable);
                }
            }
        }
    };

    @GuardedBy("this")
    private void resolve() {
        if (resolving || shutdown) {
            return;
        }
        executor.execute(resolutionRunnable);
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (resolutionTask != null) {
            resolutionTask.cancel(false);
        }
        if (timerService != null) {
            timerService = SharedResourceHolder.release(timerServiceResource, timerService);
        }
        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }
    }
}

package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.collect.Lists;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Slf4j
public class DiscoveryClientNameResolver extends NameResolver {
    private final String name;
    private final DiscoveryClient client;
    private final Attributes attributes;
    private Listener listener;

    public DiscoveryClientNameResolver(String name, DiscoveryClient client, Attributes attributes) {
        this.name = name;
        this.client = client;
        this.attributes = attributes;
    }

    @Override
    public String getServiceAuthority() {
        return name;
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;
        refresh();
    }

    @Override
    public void refresh() {
        List<List<ResolvedServerInfo>> serversList = Lists.newArrayList();
        for (ServiceInstance serviceInstance : client.getInstances(name)) {
            List<ResolvedServerInfo> servers = new ArrayList<>();
            Map<String, String> metadata = serviceInstance.getMetadata();
            if (metadata.get("grpc") != null) {
                Integer port = Integer.valueOf(metadata.get("grpc"));
                log.info("Found grpc server {} {}:{}", name, serviceInstance.getHost(), port);
                servers.add(new ResolvedServerInfo(InetSocketAddress.createUnresolved(serviceInstance.getHost(), port), Attributes.EMPTY));
            } else {
                log.error("Can not found grpc server {}", name);
            }
            serversList.add(servers);
        }
        this.listener.onUpdate(serversList, Attributes.EMPTY);
    }

    @Override
    public void shutdown() {
    }
}

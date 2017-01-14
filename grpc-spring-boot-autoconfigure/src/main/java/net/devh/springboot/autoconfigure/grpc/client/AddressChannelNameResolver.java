package net.devh.springboot.autoconfigure.grpc.client;

import com.google.common.collect.Lists;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

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
public class AddressChannelNameResolver extends NameResolver {

    private final String name;
    private final GrpcChannelProperties properties;
    private final Attributes attributes;
    private Listener listener;

    public AddressChannelNameResolver(String name, GrpcChannelProperties properties, Attributes attributes) {
        this.name = name;
        this.properties = properties;
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
    public void refresh() {
        int max = Math.max(properties.getHost().size(), properties.getPort().size());
        replace(properties.getHost(), max, GrpcChannelProperties.DEFAULT_HOST);
        replace(properties.getPort(), max, GrpcChannelProperties.DEFAULT_PORT);
        if (properties.getHost().size() != properties.getPort().size()) {
            log.error("config grpc server {} error, hosts length isn't equals ports length,hosts [{}], ports [{}]", properties.getHost(), properties.getPort());
            return;
        }
        List<List<ResolvedServerInfo>> serversList = Lists.newArrayList();
        for (int i = 0; i < properties.getHost().size(); i++) {
            List<ResolvedServerInfo> servers = new ArrayList<>();
            servers.add(new ResolvedServerInfo(InetSocketAddress.createUnresolved(properties.getHost().get(i), properties.getPort().get(i)), Attributes.EMPTY));
            serversList.add(servers);
        }
        this.listener.onUpdate(serversList, Attributes.EMPTY);
    }

    @Override
    public void shutdown() {

    }
}

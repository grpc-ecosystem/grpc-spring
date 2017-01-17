/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

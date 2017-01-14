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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.Channel;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class DiscoveryClientChannelFactory implements GrpcChannelFactory {
    private final GrpcChannelsProperties properties;
    private final DiscoveryClient client;
    private final LoadBalancer.Factory loadBalancerFactory;

    private static Map<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    public DiscoveryClientChannelFactory(GrpcChannelsProperties properties, DiscoveryClient client, LoadBalancer.Factory loadBalancerFactory) {
        this.properties = properties;
        this.client = client;
        this.loadBalancerFactory = loadBalancerFactory;
    }

    @Override
    public Channel createChannel(String name) {
        ManagedChannel channel = channelMap.get(name);
        if (channel == null) {
            ManagedChannel newChannel = ManagedChannelBuilder.forTarget(name)
                    .loadBalancerFactory(loadBalancerFactory)
                    .nameResolverFactory(new DiscoveryClientResolverFactory(client))
                    .usePlaintext(properties.getChannel(name).isPlaintext())
                    .build();
            if (channelMap.putIfAbsent(name, newChannel) == null) {
                channel = newChannel;
            } else {
                channel = channelMap.get(name);
            }
        }
        return channel;
    }
}

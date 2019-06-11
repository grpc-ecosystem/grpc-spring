/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.client.autoconfigure;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import com.google.common.collect.ImmutableList;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.nameresolver.ConfigMappedNameResolverFactory;
import net.devh.boot.grpc.client.nameresolver.DiscoveryClientResolverFactory;

@Configuration
@ConditionalOnBean(DiscoveryClient.class)
@AutoConfigureBefore(GrpcClientAutoConfiguration.class)
@SuppressWarnings("deprecation")
public class GrpcDiscoveryClientAutoConfiguration {

    @ConditionalOnMissingBean
    @Lazy // Not needed for InProcessChannelFactories
    @Bean
    @Primary
    // TODO: Remove the deprecated NameResolverProvider.asFactory() and CompositeNameResolverFactory in v2.6.0
    NameResolver.Factory grpcNameResolverProviderWithDiscovery(final GrpcChannelsProperties channelProperties,
            final DiscoveryClientResolverFactory discoveryClientResolverFactory) {
        final List<NameResolver.Factory> factories = ImmutableList.<NameResolver.Factory>builder()
                .add(discoveryClientResolverFactory)
                .add(NameResolverProvider.asFactory())
                .build();
        return new ConfigMappedNameResolverFactory(channelProperties,
                new net.devh.boot.grpc.client.nameresolver.CompositeNameResolverFactory(
                        DiscoveryClientResolverFactory.DISCOVERY_SCHEME, factories),
                DiscoveryClientResolverFactory.DISCOVERY_DEFAULT_URI_MAPPER);
    }

    @ConditionalOnMissingBean
    @Lazy // Not needed for InProcessChannelFactories
    @Bean
    DiscoveryClientResolverFactory grpcDiscoveryClientResolverFactory(final DiscoveryClient client) {
        return new DiscoveryClientResolverFactory(client);
    }

}

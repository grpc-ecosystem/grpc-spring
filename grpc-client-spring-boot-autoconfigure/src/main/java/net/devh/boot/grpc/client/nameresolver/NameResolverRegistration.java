/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
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
 */

package net.devh.boot.grpc.client.nameresolver;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;

import com.google.common.collect.ImmutableList;

import io.grpc.NameResolverProvider;
import io.grpc.NameResolverRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * The NameResolverRegistration manages the registration and de-registration of Spring managed name resolvers.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class NameResolverRegistration implements DisposableBean {

    private final List<NameResolverRegistry> registries = new ArrayList<>(1);
    private final List<NameResolverProvider> providers;

    /**
     * Creates a new NameResolverRegistration with the given list of providers.
     *
     * @param providers The providers that should be managed.
     */
    public NameResolverRegistration(List<NameResolverProvider> providers) {
        this.providers = providers == null ? ImmutableList.of() : ImmutableList.copyOf(providers);
    }

    /**
     * Register all NameResolverProviders in the given registry and store a reference to it for later de-registration.
     *
     * @param registry The registry to add the providers to.
     */
    public void register(NameResolverRegistry registry) {
        this.registries.add(registry);
        for (NameResolverProvider provider : this.providers) {
            try {
                registry.register(provider);
                log.debug("{} is available -> Added to the NameResolverRegistry", provider);
            } catch (IllegalArgumentException e) {
                log.debug("{} is not available -> Not added to the NameResolverRegistry", provider);
            }
        }
    }

    @Override
    public void destroy() {
        for (NameResolverRegistry registry : this.registries) {
            for (NameResolverProvider provider : this.providers) {
                registry.deregister(provider);
                log.debug("{} was removed from the NameResolverRegistry", provider);
            }
        }
        this.registries.clear();
    }

}

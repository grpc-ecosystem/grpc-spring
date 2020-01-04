/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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
                log.info("{} is available -> Added to the NameResolverRegistry", provider);
            } catch (IllegalArgumentException e) {
                log.info("{} is not available -> Not added to the NameResolverRegistry", provider);
            }
        }
    }

    @Override
    public void destroy() {
        for (NameResolverRegistry registry : this.registries) {
            for (NameResolverProvider provider : this.providers) {
                registry.deregister(provider);
                log.info("{} was removed from the NameResolverRegistry", provider);
            }
        }
        this.registries.clear();
    }

}

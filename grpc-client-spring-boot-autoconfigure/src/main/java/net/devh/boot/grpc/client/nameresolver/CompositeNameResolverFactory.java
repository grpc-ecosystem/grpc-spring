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

package net.devh.boot.grpc.client.nameresolver;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.grpc.NameResolver;
import io.grpc.NameResolver.Helper;
import lombok.extern.slf4j.Slf4j;

/**
 * A composite of multiple name resolvers factories. The given name resolver factories will be tried in the order they
 * are defined in and the first non null result will be returned. If no factory returns a non-null result then null will
 * be returned.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class CompositeNameResolverFactory extends NameResolver.Factory {

    private final List<NameResolver.Factory> factories;
    private final String defaultScheme;

    /**
     * Creates a new composite name resolver factory.
     *
     * @param defaultScheme The default scheme to use, if no scheme is specified.
     * @param factories The factories used to resolve the address.
     */
    public CompositeNameResolverFactory(final String defaultScheme, final List<NameResolver.Factory> factories) {
        this.factories = ImmutableList.copyOf(requireNonNull(factories, "factories"));
        this.defaultScheme = requireNonNull(defaultScheme, "defaultScheme");
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(final URI targetUri, final Helper helper) {
        log.debug("Trying to create new name resolver for: {}", targetUri);
        for (final NameResolver.Factory factory : this.factories) {
            log.debug("- Attempting with: {}", factory);
            final NameResolver resolver = factory.newNameResolver(targetUri, helper);
            if (resolver != null) {
                return resolver;
            }
        }
        log.info("Could not find name resolver for {}", targetUri);
        return null;
    }

    @Override
    public String getDefaultScheme() {
        return this.defaultScheme;
    }

    @Override
    public String toString() {
        return "CompositeNameResolverFactory [defaultScheme=" + this.defaultScheme +
                ", factories=" + this.factories + "]";
    }

}

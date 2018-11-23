/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.client.channelfactory;

import java.net.URI;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;

/**
 * A name resolver factory that will create an {@link DiscoveryClientNameResolver} based on the target uri.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class DiscoveryClientResolverFactory extends NameResolverProvider {

    private final DiscoveryClient client;
    private final Consumer<DiscoveryClientNameResolver> nameResolverManager;

    public DiscoveryClientResolverFactory(final DiscoveryClient client,
            final Consumer<DiscoveryClientNameResolver> nameResolverManager) {
        this.client = client;
        this.nameResolverManager = nameResolverManager;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(final URI targetUri, final Attributes params) {
        final DiscoveryClientNameResolver discoveryClientNameResolver =
                new DiscoveryClientNameResolver(targetUri.toString(),
                        this.client, params, GrpcUtil.TIMER_SERVICE, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
        this.nameResolverManager.accept(discoveryClientNameResolver);
        return discoveryClientNameResolver;
    }

    @Override
    public String getDefaultScheme() {
        return "discoveryClient";
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

}

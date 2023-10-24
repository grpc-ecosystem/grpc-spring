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

import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

/**
 * A name resolver provider that will create a {@link NameResolver} with static addresses. This factory uses the
 * {@link #STATIC_SCHEME "static" scheme}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class StaticNameResolverProvider extends NameResolverProvider {

    /**
     * The constant containing the scheme that will be used by this factory.
     */
    public static final String STATIC_SCHEME = "static";

    private static final Pattern PATTERN_COMMA = Pattern.compile(",");

    @Nullable
    @Override
    public NameResolver newNameResolver(final URI targetUri, final NameResolver.Args args) {
        if (STATIC_SCHEME.equals(targetUri.getScheme())) {
            return of(targetUri.getAuthority(), args.getDefaultPort());
        }
        return null;
    }

    /**
     * Creates a new {@link NameResolver} for the given authority and attributes.
     *
     * @param targetAuthority The authority to connect to.
     * @param defaultPort The default port to use, if none is specified.
     * @return The newly created name resolver for the given target.
     */
    private NameResolver of(final String targetAuthority, int defaultPort) {
        requireNonNull(targetAuthority, "targetAuthority");
        // Determine target ips
        final String[] hosts = PATTERN_COMMA.split(targetAuthority);
        final List<EquivalentAddressGroup> targets = new ArrayList<>(hosts.length);
        for (final String host : hosts) {
            final URI uri = URI.create("//" + host);
            int port = uri.getPort();
            if (port == -1) {
                port = defaultPort;
            }
            targets.add(new EquivalentAddressGroup(new InetSocketAddress(uri.getHost(), port)));
        }
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one target, but was: " + targetAuthority);
        }
        return new StaticNameResolver(targetAuthority, targets);
    }

    @Override
    public String getDefaultScheme() {
        return STATIC_SCHEME;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 4; // Less important than DNS
    }

    @Override
    public String toString() {
        return "StaticNameResolverProvider [scheme=" + getDefaultScheme() + "]";
    }

}

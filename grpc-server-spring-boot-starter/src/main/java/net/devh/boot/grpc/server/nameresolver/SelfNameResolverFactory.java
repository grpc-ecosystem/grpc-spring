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

package net.devh.boot.grpc.server.nameresolver;

import java.net.URI;

import io.grpc.NameResolver;
import io.grpc.NameResolver.Args;
import io.grpc.NameResolverProvider;
import net.devh.boot.grpc.server.config.GrpcServerProperties;

/**
 * A name resolver factory that will create a {@link SelfNameResolverFactory} based on the target uri.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
// Do not add this to the NameResolverProvider service loader list
public class SelfNameResolverFactory extends NameResolverProvider {

    /**
     * The constant containing the scheme that will be used by this factory.
     */
    public static final String SELF_SCHEME = "self";

    private final GrpcServerProperties properties;

    /**
     * Creates a new SelfNameResolverFactory that uses the given properties.
     *
     * @param properties The properties used to resolve this server's address.
     */
    public SelfNameResolverFactory(final GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public NameResolver newNameResolver(final URI targetUri, final Args args) {
        if (SELF_SCHEME.equals(targetUri.getScheme())) {
            return new SelfNameResolver(this.properties, args);
        }
        return null;
    }

    @Override
    public String getDefaultScheme() {
        return SELF_SCHEME;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 0; // Lowest priority
    }

    @Override
    public String toString() {
        return "SelfNameResolverFactory [scheme=" + getDefaultScheme() + "]";
    }

}

/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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
        if (SELF_SCHEME.equals(targetUri.getScheme()) || targetUri.toString().equals(SELF_SCHEME)) {
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

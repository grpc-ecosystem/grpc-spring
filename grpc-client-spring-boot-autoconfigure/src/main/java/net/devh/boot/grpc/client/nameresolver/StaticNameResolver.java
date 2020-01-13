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

import static java.util.Objects.requireNonNull;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

/**
 * A {@link NameResolver} that will always respond with a static set of target addresses.
 */
public class StaticNameResolver extends NameResolver {

    private final String authority;
    private final ResolutionResult result;

    /**
     * Creates a static name resolver with only a single target server.
     *
     * @param authority The authority this name resolver was created for.
     * @param target The target address of the server to use.
     */
    public StaticNameResolver(final String authority, final EquivalentAddressGroup target) {
        this(authority, ImmutableList.of(requireNonNull(target, "target")));
    }

    /**
     * Creates a static name resolver with multiple target servers.
     *
     * @param authority The authority this name resolver was created for.
     * @param targets The target addresses of the servers to use.
     */
    public StaticNameResolver(final String authority, final Collection<EquivalentAddressGroup> targets) {
        this.authority = requireNonNull(authority, "authority");
        if (requireNonNull(targets, "targets").isEmpty()) {
            throw new IllegalArgumentException("Must have at least one target");
        }
        this.result = ResolutionResult.newBuilder()
                .setAddresses(ImmutableList.copyOf(targets))
                .build();
    }

    /**
     * Creates a static name resolver with multiple target servers.
     *
     * @param authority The authority this name resolver was created for.
     * @param result The resolution result to use..
     */
    public StaticNameResolver(final String authority, final ResolutionResult result) {
        this.authority = requireNonNull(authority, "authority");
        this.result = requireNonNull(result, "result");
    }

    @Override
    public String getServiceAuthority() {
        return this.authority;
    }

    @Override
    public void start(final Listener2 listener) {
        listener.onResult(this.result);
    }

    @Override
    public void refresh() {
        // Does nothing
    }

    @Override
    public void shutdown() {
        // Does nothing
    }

    @Override
    public String toString() {
        return "StaticNameResolver [authority=" + this.authority + ", result=" + this.result + "]";
    }

}

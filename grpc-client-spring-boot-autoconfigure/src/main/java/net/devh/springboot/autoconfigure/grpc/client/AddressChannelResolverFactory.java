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

package net.devh.springboot.autoconfigure.grpc.client;

import java.net.URI;

import javax.annotation.Nullable;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;

/**
 * A name resolver factory that will create an {@link AddressChannelNameResolver} based on the target uri.
 * 
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class AddressChannelResolverFactory extends NameResolverProvider {

    private final GrpcChannelsProperties properties;

    public AddressChannelResolverFactory(GrpcChannelsProperties properties) {
        this.properties = properties;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        return new AddressChannelNameResolver(targetUri.toString(), properties.getChannel(targetUri.toString()), params,
                GrpcUtil.SHARED_CHANNEL_EXECUTOR);
    }

    @Override
    public String getDefaultScheme() {
        return "address";
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

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

package net.devh.boot.grpc.server.security.check;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;

import io.grpc.MethodDescriptor;

/**
 * A {@link SecurityMetadataSource} for grpc requests.
 *
 * <p>
 * <b>Note:</b> The authorization checking via this metadata source will only be enabled, if both an
 * {@link AccessDecisionVoter} and a {@link GrpcSecurityMetadataSource} are present in the application context.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public interface GrpcSecurityMetadataSource extends SecurityMetadataSource {

    /**
     * Accesses the {@code ConfigAttribute}s that apply to a given secure object.
     *
     * @param method The grpc method being secured.
     * @return The attributes that apply to the passed in secured object. Should return an empty collection if there are
     *         no applicable attributes.
     */
    Collection<ConfigAttribute> getAttributes(final MethodDescriptor<?, ?> method);

}

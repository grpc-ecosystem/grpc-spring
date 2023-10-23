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

package net.devh.boot.grpc.server.security.check;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;

import io.grpc.ServerCall;

/**
 * Abstract implementation of {@link GrpcSecurityMetadataSource} which resolves the secured object type to a
 * {@link ServerCall}.
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
public abstract class AbstractGrpcSecurityMetadataSource implements GrpcSecurityMetadataSource {

    @Override
    public final Collection<ConfigAttribute> getAttributes(final Object object) throws IllegalArgumentException {
        if (object instanceof ServerCall) {
            return getAttributes((ServerCall<?, ?>) object);
        }
        throw new IllegalArgumentException("Object must be a ServerCall");
    }

    @Override
    public final boolean supports(final Class<?> clazz) {
        return ServerCall.class.isAssignableFrom(clazz);
    }

}

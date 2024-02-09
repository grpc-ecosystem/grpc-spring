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

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;

import io.grpc.ServerCall;

/**
 * A {@link SecurityMetadataSource} for grpc requests.
 *
 * <p>
 * <b>Note:</b> The authorization checking via this metadata source will only be enabled, if both an
 * {@link AccessDecisionVoter} and a {@link GrpcSecurityMetadataSource} are present in the application context.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
public interface GrpcSecurityMetadataSource extends SecurityMetadataSource {

    /**
     * Accesses the {@code ConfigAttribute}s that apply to a given secure object.
     *
     * @param call The grpc call being secured.
     * @return The attributes that apply to the passed in secured object. Should return an empty collection if there are
     *         no applicable attributes.
     */
    Collection<ConfigAttribute> getAttributes(final ServerCall<?, ?> call);

}

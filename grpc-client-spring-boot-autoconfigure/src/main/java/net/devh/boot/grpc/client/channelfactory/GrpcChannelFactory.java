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

package net.devh.boot.grpc.client.channelfactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;

/**
 * This factory creates grpc {@link Channel}s for a given service name. Implementations are encouraged to utilize
 * connection pooling and thus {@link #close() close} should be called before disposing it.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public interface GrpcChannelFactory extends AutoCloseable {

    /**
     * Creates a new channel for the given service name. The returned channel will use all globally registered
     * {@link ClientInterceptor}s.
     *
     * <p>
     * <b>Note:</b> The underlying implementation might reuse existing {@link ManagedChannel}s allow connection reuse.
     * </p>
     *
     * @param name The name of the service.
     * @return The newly created channel for the given service.
     */
    default Channel createChannel(final String name) {
        return createChannel(name, Collections.emptyList());
    }

    /**
     * Creates a new channel for the given service name. The returned channel will use all globally registered
     * {@link ClientInterceptor}s.
     *
     * <p>
     * <b>Note:</b> The underlying implementation might reuse existing {@link ManagedChannel}s allow connection reuse.
     * </p>
     *
     * <p>
     * <b>Note:</b> The given interceptors will be appended to the global interceptors and applied using
     * {@link ClientInterceptors#interceptForward(Channel, ClientInterceptor...)}.
     * </p>
     *
     * @param name The name of the service.
     * @param interceptors A list of additional client interceptors that should be added to the channel.
     * @return The newly created channel for the given service.
     */
    default Channel createChannel(final String name, final List<ClientInterceptor> interceptors) {
        return createChannel(name, interceptors, false);
    }

    /**
     * Creates a new channel for the given service name. The returned channel will use all globally registered
     * {@link ClientInterceptor}s.
     *
     * <p>
     * <b>Note:</b> The underlying implementation might reuse existing {@link ManagedChannel}s allow connection reuse.
     * </p>
     *
     * <p>
     * <b>Note:</b> The given interceptors will be appended to the global interceptors and applied using
     * {@link ClientInterceptors#interceptForward(Channel, ClientInterceptor...)}.
     * </p>
     *
     * @param name The name of the service.
     * @param interceptors A list of additional client interceptors that should be added to the channel.
     * @param sortInterceptors Whether the interceptors (both global and custom) should be sorted before being applied.
     * @return The newly created channel for the given service.
     */
    Channel createChannel(String name, List<ClientInterceptor> interceptors, boolean sortInterceptors);

    /**
     * Gets an unmodifiable map that contains the names of the created channel with their current
     * {@link ConnectivityState}. This method will return an empty map, if the feature is not supported.
     *
     * @return A map with the channel names and their connectivity state.
     */
    default Map<String, ConnectivityState> getConnectivityState() {
        return Collections.emptyMap();
    }

    @Override
    void close();

}

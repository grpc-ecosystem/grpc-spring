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
     * @return The newly created channel for the given service. Might return null if the channel was intentionally
     *         skipped.
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
     * @return The newly created channel for the given service. Might return null if the channel was intentionally
     *         skipped.
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
     * @return The newly created channel for the given service. Might return null if the channel was intentionally
     *         skipped.
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

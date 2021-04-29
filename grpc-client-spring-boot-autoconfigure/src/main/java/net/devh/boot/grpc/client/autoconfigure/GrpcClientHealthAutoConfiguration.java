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

package net.devh.boot.grpc.client.autoconfigure;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.google.common.collect.ImmutableMap;

import io.grpc.ConnectivityState;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;

/**
 * Auto configuration class for Spring-Boot. This allows zero config client health status updates for gRPC services.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(GrpcClientAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
public class GrpcClientHealthAutoConfiguration {

    /**
     * Creates a HealthIndicator based on the channels' {@link ConnectivityState}s from the underlying
     * {@link GrpcChannelFactory}.
     *
     * @param factory The factory to derive the connectivity states from.
     * @return A health indicator bean, that uses the following assumption
     *         <code>DOWN == states.contains(TRANSIENT_FAILURE)</code>.
     */
    @Bean
    @Lazy
    public HealthIndicator grpcChannelHealthIndicator(final GrpcChannelFactory factory) {
        return () -> {
            final ImmutableMap<String, ConnectivityState> states = ImmutableMap.copyOf(factory.getConnectivityState());
            final Health.Builder health;
            if (states.containsValue(ConnectivityState.TRANSIENT_FAILURE)) {
                health = Health.outOfService();
            } else {
                health = Health.up();
            }
            return health.withDetails(states)
                    .build();
        };
    }

}

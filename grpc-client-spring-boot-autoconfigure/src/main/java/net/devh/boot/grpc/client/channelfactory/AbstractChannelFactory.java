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

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;

import org.springframework.util.unit.DataSize;

import com.google.common.collect.Lists;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelProperties.Security;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.config.NegotiationType;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;

/**
 * This abstract channel factory contains some shared code for other {@link GrpcChannelFactory}s. This class utilizes
 * connection pooling and thus needs to be {@link #close() closed} after usage.
 *
 * @param <T> The type of builder used by this channel factory.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @since 5/17/16
 */
@Slf4j
public abstract class AbstractChannelFactory<T extends ManagedChannelBuilder<T>> implements GrpcChannelFactory {

    private final GrpcChannelsProperties properties;
    protected final GlobalClientInterceptorRegistry globalClientInterceptorRegistry;
    protected final List<GrpcChannelConfigurer> channelConfigurers;
    /**
     * According to <a href="https://groups.google.com/forum/#!topic/grpc-io/-jA_JCiugM8">Thread safety in Grpc java
     * clients</a>: {@link ManagedChannel}s should be reused to allow connection reuse.
     */
    @GuardedBy("this")
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final Map<String, ConnectivityState> channelStates = new ConcurrentHashMap<>();
    private boolean shutdown = false;

    /**
     * Creates a new AbstractChannelFactory with eager initialized references.
     *
     * @param properties The properties for the channels to create.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     * @param channelConfigurers The channel configurers to use. Can be empty.
     */
    public AbstractChannelFactory(final GrpcChannelsProperties properties,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        this.properties = requireNonNull(properties, "properties");
        this.globalClientInterceptorRegistry =
                requireNonNull(globalClientInterceptorRegistry, "globalClientInterceptorRegistry");
        this.channelConfigurers = requireNonNull(channelConfigurers, "channelConfigurers");
    }

    @Override
    public final Channel createChannel(final String name) {
        return createChannel(name, Collections.emptyList());
    }

    @Override
    public Channel createChannel(final String name, final List<ClientInterceptor> customInterceptors,
            final boolean sortInterceptors) {
        final Channel channel;
        synchronized (this) {
            if (this.shutdown) {
                throw new IllegalStateException("GrpcChannelFactory is already closed!");
            }
            channel = this.channels.computeIfAbsent(name, this::newManagedChannel);
        }
        final List<ClientInterceptor> interceptors =
                Lists.newArrayList(this.globalClientInterceptorRegistry.getClientInterceptors());
        interceptors.addAll(customInterceptors);
        if (sortInterceptors) {
            this.globalClientInterceptorRegistry.sortInterceptors(interceptors);
        }
        return ClientInterceptors.interceptForward(channel, interceptors);
    }

    /**
     * Creates a new {@link ManagedChannelBuilder} for the given client name.
     *
     * @param name The name to create the channel builder for.
     * @return The newly created channel builder.
     */
    protected abstract T newChannelBuilder(String name);

    /**
     * Creates a new {@link ManagedChannel} for the given client name. The name will be used to determine the properties
     * for the new channel. The calling method is responsible for lifecycle management of the created channel.
     * ManagedChannels should be reused if possible to allow connection reuse.
     *
     * @param name The name to create the channel for.
     * @return The newly created channel.
     * @see #newChannelBuilder(String)
     * @see #configure(ManagedChannelBuilder, String)
     */
    protected ManagedChannel newManagedChannel(final String name) {
        final T builder = newChannelBuilder(name);
        configure(builder, name);
        final ManagedChannel channel = builder.build();
        final Duration timeout = this.properties.getChannel(name).getImmediateConnectTimeout();
        if (!timeout.isZero()) {
            connectOnStartup(name, channel, timeout);
        }
        watchConnectivityState(name, channel);
        return channel;
    }

    /**
     * Gets the channel properties for the given client name.
     *
     * @param name The client name to use.
     * @return The properties for the given client name.
     */
    protected final GrpcChannelProperties getPropertiesFor(final String name) {
        return this.properties.getChannel(name);
    }

    /**
     * Configures the given channel builder. This method can be overwritten to add features that are not yet supported
     * by this library.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configure(final T builder, final String name) {
        configureKeepAlive(builder, name);
        configureSecurity(builder, name);
        configureLimits(builder, name);
        configureCompression(builder, name);
        for (final GrpcChannelConfigurer channelConfigurer : this.channelConfigurers) {
            channelConfigurer.accept(builder, name);
        }
    }

    /**
     * Configures the keep alive options that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureKeepAlive(final T builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        if (properties.isEnableKeepAlive()) {
            builder.keepAliveTime(properties.getKeepAliveTime().toNanos(), TimeUnit.NANOSECONDS)
                    .keepAliveTimeout(properties.getKeepAliveTimeout().toNanos(), TimeUnit.NANOSECONDS)
                    .keepAliveWithoutCalls(properties.isKeepAliveWithoutCalls());
        }
    }

    /**
     * Configures the security options that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureSecurity(final T builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        final Security security = properties.getSecurity();

        if (properties.getNegotiationType() != NegotiationType.TLS // non-default
                || isNonNullAndNonBlank(security.getAuthorityOverride())
                || security.getCertificateChain() != null
                || security.getPrivateKey() != null
                || security.getTrustCertCollection() != null) {
            throw new IllegalStateException(
                    "Security is configured but this implementation does not support security!");
        }
    }

    /**
     * Checks whether the given value is non null and non blank.
     *
     * @param value The value to check.
     * @return True, if the given value was neither null nor blank. False otherwise.
     */
    protected boolean isNonNullAndNonBlank(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Configures limits such as max message sizes that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureLimits(final T builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        final DataSize maxInboundMessageSize = properties.getMaxInboundMessageSize();
        if (maxInboundMessageSize != null) {
            builder.maxInboundMessageSize((int) maxInboundMessageSize.toBytes());
        }
    }

    /**
     * Configures the compression options that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureCompression(final T builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        if (properties.isFullStreamDecompression()) {
            builder.enableFullStreamDecompression();
        }
    }

    @Override
    public Map<String, ConnectivityState> getConnectivityState() {
        return Collections.unmodifiableMap(this.channelStates);
    }

    /**
     * Watch the given channel for connectivity changes.
     *
     * @param name The name of the channel in the state overview.
     * @param channel The channel to watch the state of.
     */
    protected void watchConnectivityState(final String name, final ManagedChannel channel) {
        final ConnectivityState state = channel.getState(false);
        this.channelStates.put(name, state);
        if (state != ConnectivityState.SHUTDOWN) {
            channel.notifyWhenStateChanged(state, () -> watchConnectivityState(name, channel));
        }
    }

    private void connectOnStartup(final String name, final ManagedChannel channel, final Duration timeout) {
        log.debug("Initiating connection to channel {}", name);
        channel.getState(true);

        final CountDownLatch readyLatch = new CountDownLatch(1);
        waitForReady(channel, readyLatch);
        boolean connected;
        try {
            log.debug("Waiting for connection to channel {}", name);
            connected = !readyLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            connected = false;
        }
        if (connected) {
            throw new IllegalStateException("Can't connect to channel " + name);
        }
        log.info("Successfully connected to channel {}", name);
    }

    private void waitForReady(final ManagedChannel channel, final CountDownLatch readySignal) {
        final ConnectivityState state = channel.getState(false);
        log.debug("Waiting for ready state. Currently in {}", state);
        if (state == ConnectivityState.READY) {
            readySignal.countDown();
        } else {
            channel.notifyWhenStateChanged(state, () -> waitForReady(channel, readySignal));
        }
    }

    /**
     * Closes this channel factory and the channels created by this instance. The shutdown happens in two phases, first
     * an orderly shutdown is initiated on all channels and then the method waits for all channels to terminate. If the
     * channels don't have terminated after 60 seconds then they will be forcefully shutdown.
     */
    @Override
    @PreDestroy
    public synchronized void close() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        for (final ManagedChannel channel : this.channels.values()) {
            channel.shutdown();
        }
        try {
            final long waitLimit = System.currentTimeMillis() + 60_000; // wait 60 seconds at max
            for (final ManagedChannel channel : this.channels.values()) {
                int i = 0;
                do {
                    log.debug("Awaiting channel shutdown: {} ({}s)", channel, i++);
                } while (System.currentTimeMillis() < waitLimit && !channel.awaitTermination(1, TimeUnit.SECONDS));
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("We got interrupted - Speeding up shutdown process");
        } finally {
            for (final ManagedChannel channel : this.channels.values()) {
                if (!channel.isTerminated()) {
                    log.debug("Channel not terminated yet - force shutdown now: {} ", channel);
                    channel.shutdownNow();
                }
            }
        }
        final int channelCount = this.channels.size();
        this.channels.clear();
        this.channelStates.clear();
        log.debug("GrpcCannelFactory closed (including {} channels)", channelCount);
    }

}

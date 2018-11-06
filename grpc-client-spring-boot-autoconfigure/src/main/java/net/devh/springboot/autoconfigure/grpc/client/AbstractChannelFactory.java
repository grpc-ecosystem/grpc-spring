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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;
import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.NameResolver;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcChannelProperties.Security;

/**
 * This abstract channel factory contains some shared code for other {@link GrpcChannelFactory}s. This class utilizes
 * connection pooling and thus needs to be {@link #close() closed} after usage.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @since 5/17/16
 */
@Slf4j
public abstract class AbstractChannelFactory implements GrpcChannelFactory {

    private final GrpcChannelsProperties properties;
    private final LoadBalancer.Factory loadBalancerFactory;
    private final NameResolver.Factory nameResolverFactory;
    private final GlobalClientInterceptorRegistry globalClientInterceptorRegistry;
    /**
     * According to <a href="https://groups.google.com/forum/#!topic/grpc-io/-jA_JCiugM8">Thread safety in Grpc java
     * clients</a>: {@link ManagedChannel}s should be reused to allow connection reuse.
     */
    @GuardedBy("this")
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private boolean shutdown = false;

    /**
     * Creates a new AbstractChannelFactory with eager initialized references.
     *
     * @param properties The properties for the channels to create.
     * @param loadBalancerFactory The load balancer factory to use.
     * @param nameResolverFactory The name resolver factory to use.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     */
    public AbstractChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory,
            final NameResolver.Factory nameResolverFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        this.properties = properties;
        this.loadBalancerFactory = loadBalancerFactory;
        this.nameResolverFactory = nameResolverFactory;
        this.globalClientInterceptorRegistry = globalClientInterceptorRegistry;
    }

    /**
     * Creates a new AbstractChannelFactory with partially lazy initialized references.
     *
     * @param <T> The type of the actual factory class or one of its super classes.
     * @param properties The properties for the channels to create.
     * @param loadBalancerFactory The load balancer factory to use.
     * @param nameResolverFactoryCreator The function that creates the name resolver factory.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractChannelFactory> AbstractChannelFactory(final GrpcChannelsProperties properties,
            final LoadBalancer.Factory loadBalancerFactory,
            final Function<T, NameResolver.Factory> nameResolverFactoryCreator,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        this.properties = properties;
        this.loadBalancerFactory = loadBalancerFactory;
        this.nameResolverFactory = nameResolverFactoryCreator.apply((T) this);
        this.globalClientInterceptorRegistry = globalClientInterceptorRegistry;
    }

    @Override
    public final Channel createChannel(final String name) {
        return createChannel(name, Collections.emptyList());
    }

    @Override
    public Channel createChannel(final String name, final List<ClientInterceptor> customInterceptors) {
        final Channel channel;
        synchronized (this) {
            if (this.shutdown) {
                throw new IllegalStateException("GrpcChannelFactory is already closed!");
            }
            channel = this.channels.computeIfAbsent(name, this::newManagedChannel);
        }
        final List<ClientInterceptor> interceptors = Lists.newArrayList();
        final List<ClientInterceptor> globalInterceptors = this.globalClientInterceptorRegistry.getClientInterceptors();
        if (!globalInterceptors.isEmpty()) {
            interceptors.addAll(globalInterceptors);
        }
        if (!customInterceptors.isEmpty()) {
            interceptors.addAll(customInterceptors);
        }
        return ClientInterceptors.intercept(channel, interceptors);
    }

    /**
     * Creates a new {@link ManagedChannel} for the given client name. The name will be used to determine the properties
     * for the new channel. The calling method is responsible for lifecycle management of the created channel.
     * ManagedChannels should be reused if possible to allow connection reuse.
     *
     * @param name The name to create the channel for.
     * @return The newly created channel.
     * @see #configure(NettyChannelBuilder, String)
     */
    protected ManagedChannel newManagedChannel(final String name) {
        final NettyChannelBuilder builder = NettyChannelBuilder.forTarget(name)
                .loadBalancerFactory(this.loadBalancerFactory)
                .nameResolverFactory(this.nameResolverFactory);
        configure(builder, name);
        return builder.build();
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
     * Configures the given netty channel builder. This method can be overwritten to add features that are not yet
     * supported by this library.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configure(final NettyChannelBuilder builder, final String name) {
        configureKeepAlive(builder, name);
        configureSecurity(builder, name);
        configureLimits(builder, name);
        configureCompression(builder, name);
    }

    /**
     * Configures the keep alive options that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureKeepAlive(final NettyChannelBuilder builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        if (properties.isEnableKeepAlive()) {
            builder.keepAliveWithoutCalls(properties.isKeepAliveWithoutCalls())
                    .keepAliveTime(properties.getKeepAliveTime(), TimeUnit.SECONDS)
                    .keepAliveTimeout(properties.getKeepAliveTimeout(), TimeUnit.SECONDS);
        }
    }

    /**
     * Configures the security options that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureSecurity(final NettyChannelBuilder builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);

        final NegotiationType negotiationType = properties.getNegotiationType();
        builder.negotiationType(negotiationType);

        if (negotiationType != NegotiationType.PLAINTEXT) {
            final Security security = properties.getSecurity();

            final String authorityOverwrite = security.getAuthorityOverride();
            if (authorityOverwrite != null && !authorityOverwrite.isEmpty()) {
                builder.overrideAuthority(authorityOverwrite);
            }

            final SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();

            if (security.isClientAuthEnabled()) {
                final File keyCertChainFile = toCheckedFile("keyCertChain", security.getCertificateChainPath());
                final File privateKeyFile = toCheckedFile("privateKey", security.getPrivateKeyPath());
                sslContextBuilder.keyManager(keyCertChainFile, privateKeyFile);
            }

            final String trustCertCollectionPath = security.getTrustCertCollectionPath();
            if (trustCertCollectionPath != null && !trustCertCollectionPath.isEmpty()) {
                final File trustCertCollectionFile = toCheckedFile("trustCertCollection", trustCertCollectionPath);
                sslContextBuilder.trustManager(trustCertCollectionFile);
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (final SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc client", e);
            }
        }
    }

    /**
     * Converts the given path to a file. This method checks that the file exists and refers to a file.
     *
     * @param context The context for what the file is used. This value will be used in case of exceptions.
     * @param path The path of the file to use.
     * @return The file instance created with the given path.
     */
    private File toCheckedFile(final String context, final String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException(context + " path cannot be null or blank");
        }
        final File file = new File(path);
        if (!file.isFile()) {
            String message =
                    context + " file does not exist or path does not refer to a file: '" + file.getPath() + "'";
            if (!file.isAbsolute()) {
                message += " (" + file.getAbsolutePath() + ")";
            }
            throw new IllegalArgumentException(message);
        }
        return file;
    }

    /**
     * Configures limits such as max message sizes that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureLimits(final NettyChannelBuilder builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        final Integer maxInboundMessageSize = properties.getMaxInboundMessageSize();
        if (maxInboundMessageSize != null) {
            builder.maxInboundMessageSize(maxInboundMessageSize);
        }
    }

    /**
     * Configures the compression options that should be used by the channel.
     *
     * @param builder The channel builder to configure.
     * @param name The name of the client to configure.
     */
    protected void configureCompression(final NettyChannelBuilder builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);
        if (properties.isFullStreamDecompression()) {
            builder.enableFullStreamDecompression();
        }
    }

    /**
     * Closes this channel factory and the channels created by this instance. The shutdown happens in two phases, first
     * an orderly shutdown is initiated on all channels and then the method waits for all channels to terminate.
     */
    @Override
    @PreDestroy
    public synchronized void close() throws InterruptedException {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        for (final ManagedChannel channel : this.channels.values()) {
            channel.shutdown();
        }
        for (final ManagedChannel channel : this.channels.values()) {
            int i = 0;
            do {
                log.debug("Awaiting channel shutdown: {} ({}s)", channel, i++);
            } while (!channel.awaitTermination(1, TimeUnit.SECONDS));
        }
        final int channelCount = this.channels.size();
        this.channels.clear();
        log.debug("GrpcCannelFactory closed (including {} channels)", channelCount);
    }

}

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

package net.devh.boot.grpc.server.serverfactory;

import static java.util.Objects.requireNonNull;
import static net.devh.boot.grpc.common.util.GrpcUtils.DOMAIN_SOCKET_ADDRESS_PREFIX;
import static net.devh.boot.grpc.server.config.GrpcServerProperties.ANY_IP_ADDRESS;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.core.io.Resource;

import com.google.common.net.InetAddresses;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import net.devh.boot.grpc.common.security.KeyStoreUtils;
import net.devh.boot.grpc.common.util.GrpcUtils;
import net.devh.boot.grpc.server.config.ClientAuth;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.config.GrpcServerProperties.Security;

/**
 * Factory for shaded netty based grpc servers.
 *
 * @author Michael (yidongnan@gmail.com)
 */
public class ShadedNettyGrpcServerFactory
        extends AbstractGrpcServerFactory<io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder> {

    /**
     * Creates a new shaded netty server factory with the given properties.
     *
     * @param properties The properties used to configure the server.
     * @param serverConfigurers The server configurers to use. Can be empty.
     */
    public ShadedNettyGrpcServerFactory(final GrpcServerProperties properties,
            final List<GrpcServerConfigurer> serverConfigurers) {
        super(properties, serverConfigurers);
    }

    @Override
    protected NettyServerBuilder newServerBuilder() {
        final String address = getAddress();
        final int port = getPort();
        if (address.startsWith(DOMAIN_SOCKET_ADDRESS_PREFIX)) {
            final String path = GrpcUtils.extractDomainSocketAddressPath(address);
            return NettyServerBuilder.forAddress(new DomainSocketAddress(path))
                    .channelType(EpollServerDomainSocketChannel.class)
                    .bossEventLoopGroup(new EpollEventLoopGroup(1))
                    .workerEventLoopGroup(new EpollEventLoopGroup());
        } else if (ANY_IP_ADDRESS.equals(address)) {
            return NettyServerBuilder.forPort(port);
        } else {
            return NettyServerBuilder.forAddress(new InetSocketAddress(InetAddresses.forString(address), port));
        }
    }

    @Override
    // Keep this in sync with NettyGrpcServerFactory#configureConnectionLimits
    protected void configureConnectionLimits(final NettyServerBuilder builder) {
        if (this.properties.getMaxConnectionIdle() != null) {
            builder.maxConnectionIdle(this.properties.getMaxConnectionIdle().toNanos(), TimeUnit.NANOSECONDS);
        }
        if (this.properties.getMaxConnectionAge() != null) {
            builder.maxConnectionAge(this.properties.getMaxConnectionAge().toNanos(), TimeUnit.NANOSECONDS);
        }
        if (this.properties.getMaxConnectionAgeGrace() != null) {
            builder.maxConnectionAgeGrace(this.properties.getMaxConnectionAgeGrace().toNanos(), TimeUnit.NANOSECONDS);
        }
    }

    @Override
    // Keep this in sync with NettyGrpcServerFactory#configureKeepAlive
    protected void configureKeepAlive(final NettyServerBuilder builder) {
        if (this.properties.isEnableKeepAlive()) {
            builder.keepAliveTime(this.properties.getKeepAliveTime().toNanos(), TimeUnit.NANOSECONDS)
                    .keepAliveTimeout(this.properties.getKeepAliveTimeout().toNanos(), TimeUnit.NANOSECONDS);
        }
        builder.permitKeepAliveTime(this.properties.getPermitKeepAliveTime().toNanos(), TimeUnit.NANOSECONDS)
                .permitKeepAliveWithoutCalls(this.properties.isPermitKeepAliveWithoutCalls());
    }

    @Override
    // Keep this in sync with NettyGrpcServerFactory#configureSecurity
    protected void configureSecurity(final NettyServerBuilder builder) {
        final Security security = this.properties.getSecurity();
        if (security.isEnabled()) {
            // Provided server certificates
            final SslContextBuilder sslContextBuilder = newServerSslContextBuilder(security);

            // Accepted client certificates
            configureAcceptedClientCertificates(security, sslContextBuilder);

            // Other configuration
            if (security.getCiphers() != null && !security.getCiphers().isEmpty()) {
                sslContextBuilder.ciphers(security.getCiphers());
            }

            if (security.getProtocols() != null && security.getProtocols().length > 0) {
                sslContextBuilder.protocols(security.getProtocols());
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (final SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc server", e);
            }
        }
    }

    /**
     * Creates a new server ssl context builder.
     *
     * @param security The security configuration to use.
     * @return The newly created SslContextBuilder.
     */
    // Keep this in sync with NettyGrpcServerFactory#newServerSslContextBuilder
    protected static SslContextBuilder newServerSslContextBuilder(final Security security) {
        try {
            final Resource privateKey = security.getPrivateKey();
            final Resource keyStore = security.getKeyStore();

            if (privateKey != null) {
                final Resource certificateChain =
                        requireNonNull(security.getCertificateChain(), "certificateChain");
                final String privateKeyPassword = security.getPrivateKeyPassword();
                try (InputStream certificateChainStream = certificateChain.getInputStream();
                        InputStream privateKeyStream = privateKey.getInputStream()) {
                    return GrpcSslContexts.forServer(certificateChainStream, privateKeyStream, privateKeyPassword);
                }

            } else if (keyStore != null) {
                final KeyManagerFactory keyManagerFactory = KeyStoreUtils.loadKeyManagerFactory(
                        security.getKeyStoreFormat(), keyStore, security.getKeyStorePassword());
                return GrpcSslContexts.configure(SslContextBuilder.forServer(keyManagerFactory));

            } else {
                throw new IllegalStateException("Neither privateKey nor keyStore configured");
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to create SSLContext (PK/Cert)", e);
        }
    }

    /**
     * Configures the client certificates accepted by the ssl context.
     *
     * @param security The security configuration to use.
     * @param sslContextBuilder The ssl context builder to configure.
     */
    // Keep this in sync with NettyGrpcServerFactory#configureAcceptedClientCertificates
    protected static void configureAcceptedClientCertificates(
            final Security security,
            final SslContextBuilder sslContextBuilder) {

        if (security.getClientAuth() != ClientAuth.NONE) {
            sslContextBuilder.clientAuth(of(security.getClientAuth()));

            try {
                final Resource trustCertCollection = security.getTrustCertCollection();
                final Resource trustStore = security.getTrustStore();

                if (trustCertCollection != null) {
                    try (InputStream trustCertCollectionStream = trustCertCollection.getInputStream()) {
                        sslContextBuilder.trustManager(trustCertCollectionStream);
                    }

                } else if (trustStore != null) {
                    final TrustManagerFactory trustManagerFactory = KeyStoreUtils.loadTrustManagerFactory(
                            security.getTrustStoreFormat(), trustStore, security.getTrustStorePassword());
                    sslContextBuilder.trustManager(trustManagerFactory);

                } else {
                    // Use system default
                }
            } catch (final Exception e) {
                throw new IllegalArgumentException("Failed to create SSLContext (TrustStore)", e);
            }
        }
    }

    /**
     * Converts the given client auth option to netty's client auth.
     *
     * @param clientAuth The client auth option to convert.
     * @return The converted client auth option.
     */
    // Keep this in sync with NettyGrpcServerFactory#configureAcceptedClientCertificates
    protected static io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth of(final ClientAuth clientAuth) {
        switch (clientAuth) {
            case NONE:
                return io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth.NONE;
            case OPTIONAL:
                return io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth.OPTIONAL;
            case REQUIRE:
                return io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth.REQUIRE;
            default:
                throw new IllegalArgumentException("Unsupported ClientAuth: " + clientAuth);
        }
    }

}

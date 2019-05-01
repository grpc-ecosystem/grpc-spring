/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

import java.io.File;
import java.util.List;

import javax.net.ssl.SSLException;

import io.grpc.NameResolver;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelProperties.Security;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.config.NegotiationType;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;

/**
 * This channel factory creates and manages shaded netty based {@link GrpcChannelFactory}s.
 *
 * <p>
 * This class utilizes connection pooling and thus needs to be {@link #close() closed} after usage.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
// Keep this file in sync with NettyChannelFactory
public class ShadedNettyChannelFactory extends AbstractChannelFactory<NettyChannelBuilder> {

    private final NameResolver.Factory nameResolverFactory;

    /**
     * Creates a new GrpcChannelFactory for shaded netty with the given options.
     *
     * @param properties The properties for the channels to create.
     * @param nameResolverFactory The name resolver factory to use.
     * @param globalClientInterceptorRegistry The interceptor registry to use.
     * @param channelConfigurers The channel configurers to use. Can be empty.
     */
    public ShadedNettyChannelFactory(final GrpcChannelsProperties properties,
            final NameResolver.Factory nameResolverFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        super(properties, globalClientInterceptorRegistry, channelConfigurers);
        this.nameResolverFactory = requireNonNull(nameResolverFactory, "nameResolverFactory");
    }

    @Override
    protected NettyChannelBuilder newChannelBuilder(final String name) {
        return NettyChannelBuilder.forTarget(name)
                .defaultLoadBalancingPolicy(getPropertiesFor(name).getDefaultLoadBalancingPolicy())
                .nameResolverFactory(this.nameResolverFactory);
    }

    @Override
    protected void configureSecurity(final NettyChannelBuilder builder, final String name) {
        final GrpcChannelProperties properties = getPropertiesFor(name);

        final NegotiationType negotiationType = properties.getNegotiationType();
        builder.negotiationType(of(negotiationType));

        if (negotiationType == NegotiationType.TLS) {
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

            if (security.getCiphers() != null && !security.getCiphers().isEmpty()) {
                sslContextBuilder.ciphers(security.getCiphers());
            }

            if (security.getProtocols() != null && security.getProtocols().length > 0) {
                sslContextBuilder.protocols(security.getProtocols());
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (final SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc client", e);
            }
        }
    }

    /**
     * Converts the given negotiation type to netty's negotiation type.
     *
     * @param negotiationType The negotiation type to convert.
     * @return The converted negotiation type.
     */
    protected static io.grpc.netty.shaded.io.grpc.netty.NegotiationType of(final NegotiationType negotiationType) {
        switch (negotiationType) {
            case PLAINTEXT:
                return io.grpc.netty.shaded.io.grpc.netty.NegotiationType.PLAINTEXT;
            case PLAINTEXT_UPGRADE:
                return io.grpc.netty.shaded.io.grpc.netty.NegotiationType.PLAINTEXT_UPGRADE;
            case TLS:
                return io.grpc.netty.shaded.io.grpc.netty.NegotiationType.TLS;
            default:
                throw new IllegalArgumentException("Unsupported NegotiationType: " + negotiationType);
        }
    }

}

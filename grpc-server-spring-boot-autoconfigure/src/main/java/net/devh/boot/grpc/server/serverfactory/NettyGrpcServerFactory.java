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

package net.devh.boot.grpc.server.serverfactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

import javax.net.ssl.SSLException;

import com.google.common.net.InetAddresses;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.config.GrpcServerProperties.Security;

/**
 * Factory for netty based grpc servers.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public class NettyGrpcServerFactory extends AbstractGrpcServerFactory<NettyServerBuilder> {

    /**
     * Creates a new netty server factory with the given properties.
     *
     * @param properties The properties used to configure the server.
     * @param serverConfigurers The server configurers to use. Can be empty.
     */
    public NettyGrpcServerFactory(final GrpcServerProperties properties,
            final List<GrpcServerConfigurer> serverConfigurers) {
        super(properties, serverConfigurers);
    }

    @Override
    protected NettyServerBuilder newServerBuilder() {
        return NettyServerBuilder.forAddress(new InetSocketAddress(InetAddresses.forString(getAddress()), getPort()));
    }

    @Override
    protected void configureSecurity(final NettyServerBuilder builder) {
        final Security security = this.properties.getSecurity();
        if (security.isEnabled()) {
            final File certificateChainFile = toCheckedFile("certificateChain", security.getCertificateChainPath());
            final File privateKeyFile = toCheckedFile("privateKey", security.getPrivateKeyPath());
            final SslContextBuilder sslContextBuilder = GrpcSslContexts.forServer(certificateChainFile, privateKeyFile);

            if (security.getClientAuth() != ClientAuth.NONE) {
                sslContextBuilder.clientAuth(security.getClientAuth());

                final String trustCertCollectionPath = security.getTrustCertCollectionPath();
                if (trustCertCollectionPath != null && !trustCertCollectionPath.isEmpty()) {
                    final File trustCertCollectionFile = toCheckedFile("trustCertCollection", trustCertCollectionPath);
                    sslContextBuilder.trustManager(trustCertCollectionFile);
                }
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (final SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc server", e);
            }
        }
    }

}

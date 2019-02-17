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

package net.devh.boot.grpc.client.config;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.convert.DurationUnit;

import io.grpc.internal.GrpcUtil;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import lombok.Data;

/**
 * The channel properties for a single named gRPC channel or service reference.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Data
@SuppressWarnings("javadoc")
public class GrpcChannelProperties {

    public static final GrpcChannelProperties DEFAULT = new GrpcChannelProperties();

    /**
     * The target uri in the format: {@code schema:[//[authority]][/path]}. If nothing is configured then the
     * {@link io.grpc.NameResolver.Factory} will decide on the default.
     *
     * <p>
     * <b>Examples</b>
     * </p>
     *
     * <ul>
     * <li>{@code static://localhost:9090} (refers to exactly one IPv4 or IPv6 address, dependent on the jre
     * configuration, it does not check whether there is actually someone listening on that network interface)</li>
     * <li>{@code static://10.0.0.10}</li>
     * <li>{@code static://10.0.0.10,10.11.12.11}</li>
     * <li>{@code static://10.0.0.10:9090,10.0.0.11:80,10.0.0.12:1234,[::1]:8080}</li>
     * <li>{@code dns:/localhost (might refer to the IPv4 or the IPv6 address or both, dependent on the system
     * configuration, it does not check whether there is actually someone listening on that network interface)}</li>
     * <li>{@code dns:/example.com}</li>
     * <li>{@code dns:/example.com:9090}</li>
     * <li>{@code dns:///example.com:9090}</li>
     * <li>{@code discovery:/foo-service}</li>
     * <li>{@code discovery:///foo-service}</li>
     * </ul>
     *
     * @param address The address to connect to.
     * @return The address to connect to.
     */
    private URI address = null;

    /**
     * Sets the target address uri. The target uri must be in the format: {@code schema:[//[authority]][/path]}. If
     * nothing is configured then the {@link io.grpc.NameResolver.Factory} will decide on the default.
     *
     * <p>
     * <b>Examples</b>
     * </p>
     *
     * <ul>
     * <li>{@code static://localhost:9090} (refers to exactly one IPv4 or IPv6 address, dependent on the jre
     * configuration, it does not check whether there is actually someone listening on that network interface)</li>
     * <li>{@code static://10.0.0.10}</li>
     * <li>{@code static://10.0.0.10,10.11.12.11}</li>
     * <li>{@code static://10.0.0.10:9090,10.0.0.11:80,10.0.0.12:1234,[::1]:8080}</li>
     * <li>{@code dns:/localhost (might refer to the IPv4 or the IPv6 address or both, dependent on the system
     * configuration, it does not check whether there is actually someone listening on that network interface)}</li>
     * <li>{@code dns:/example.com}</li>
     * <li>{@code dns:/example.com:9090}</li>
     * <li>{@code dns:///example.com:9090}</li>
     * <li>{@code discovery:/foo-service}</li>
     * <li>{@code discovery:///foo-service}</li>
     * </ul>
     *
     * @param uri The string representation of an uri to use as target address.
     */
    public void setAddress(final String uri) {
        this.address = URI.create(uri);
    }

    /**
     * Removed property.
     *
     * @param host Removed.
     * @deprecated Use {@link #setAddress(String)} instead.
     */
    @Deprecated
    public void setHost(final String host) {
        throw new UnsupportedOperationException(
                "Use the 'address' attribute with 'static://host1:port1,...,hostn:portn' instead");
    }

    /**
     * Removed property.
     *
     * @param hosts Removed.
     * @deprecated Use {@link #setAddress(String)} instead.
     */
    @Deprecated
    public void setHost(final List<String> hosts) {
        throw new UnsupportedOperationException(
                "Use the 'address' attribute with 'static://host1:port1,...,hostn:portn' instead");
    }

    /**
     * Removed property.
     *
     * @param port Removed.
     * @deprecated Use {@link #setAddress(String)} instead.
     */
    @Deprecated
    public void setPort(final String port) {
        throw new UnsupportedOperationException(
                "Use the 'address' attribute with 'static://host1:port1,...,hostn:portn' instead");
    }

    /**
     * Removed property.
     *
     * @param ports Removed.
     * @deprecated Use {@link #setAddress(String)} instead.
     */
    @Deprecated
    public void setPort(final List<String> ports) {
        throw new UnsupportedOperationException(
                "Use the 'address' attribute with 'static://host1:port1,...,hostn:portn' instead");
    }

    /**
     * Setting to enable keepAlive. Default to {@code false}.
     *
     * @param enableKeepAlive Whether keep alive should be enabled.
     * @return True, if keep alive should be enabled. False otherwise.
     */
    private boolean enableKeepAlive = false;

    /**
     * The default delay before we send a keepAlives. Defaults to {@code 60s}. Default unit {@link ChronoUnit#SECONDS
     * SECONDS}.
     *
     * @see #setEnableKeepAlive(boolean)
     * @see NettyServerBuilder#keepAliveTime(long, TimeUnit)
     *
     * @param keepAliveTime The new default delay before sending keepAlives.
     * @return The default delay before sending keepAlives.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration keepAliveTime = Duration.of(60, ChronoUnit.SECONDS);

    /**
     * The default timeout for a keepAlives ping request. Defaults to {@code 20s}. Default unit
     * {@link ChronoUnit#SECONDS SECONDS}.
     *
     * @see #setEnableKeepAlive(boolean)
     * @see NettyServerBuilder#keepAliveTimeout(long, TimeUnit)
     *
     * @param keepAliveTimeout Sets the default timeout for a keepAlives ping request.
     * @return The default timeout for a keepAlives ping request.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration keepAliveTimeout = Duration.of(20, ChronoUnit.SECONDS);

    /**
     * Sets whether keepAlive will be performed when there are no outstanding RPC on a connection. Defaults to
     * {@code false}.
     *
     * @see #setEnableKeepAlive(boolean)
     * @see NettyChannelBuilder#keepAliveWithoutCalls(boolean)
     *
     * @param keepAliveWithoutCalls whether keepAlive will be performed when there are no outstanding RPC on a
     *        connection.
     * @return True, if keepAlives should be performed even when there are no RPCs. False otherwise.
     */
    private boolean keepAliveWithoutCalls = false;

    /**
     * The maximum message size in bytes allowed to be received by the channel. If not set ({@code null}) then it will
     * default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE gRPC's default}. If set to {@code -1} then it will use the
     * highest possible limit (not recommended).
     *
     * @param maxInboundMessageSize The maximum message size.
     * @return The maximum message size allowed.
     */
    private Integer maxInboundMessageSize = null;

    private boolean fullStreamDecompression = false;

    /**
     * The negotiation type to use on the connection. Either of {@link NegotiationType#TLS TLS} (recommended),
     * {@link NegotiationType#PLAINTEXT_UPGRADE PLAINTEXT_UPGRADE} or {@link NegotiationType#PLAINTEXT PLAINTEXT}.
     * Defaults to TLS.
     *
     * @param negotiationType The negotiation type to use.
     * @return The negotiation type that the channel will use.
     */
    private NegotiationType negotiationType = NegotiationType.TLS;

    /**
     * Security options for transport security.
     *
     * @return The security options for transport security.
     */
    private final Security security = new Security();

    @Data
    public static class Security {

        /**
         * Flag that controls whether client can authenticate using certificates. Defaults to {@code false}.
         *
         * @param clientAuthEnabled Whether the client can authenticate itself using certificates.
         * @return True, if the client can authenticate itself using certificates.
         */
        private boolean clientAuthEnabled = false;

        /**
         * Path to SSL certificate chain. Required if {@link #isClientAuthEnabled()} is true.
         *
         * @see SslContextBuilder#keyManager(File, File)
         *
         * @param certificateChainPath The path to the certificate chain.
         * @return The path to the certificate chain or null, if security is not enabled.
         */
        private String certificateChainPath = null;

        /**
         * Path to private key. Required if {@link #isClientAuthEnabled} is true.
         *
         * @see SslContextBuilder#keyManager(File, File)
         *
         * @param privateKeyPath The path to the private key.
         * @return The path to the private key or null, if security is not enabled.
         */
        private String privateKeyPath = null;

        /**
         * Path to the trusted certificate collection. If {@code null} or empty it will use the system's default
         * collection (Default). This collection will be used to verify client certificates.
         *
         * @see SslContextBuilder#trustManager(File)
         *
         * @param trustCertCollectionPath The path to the trusted certificate collection.
         * @return The path to the trusted certificate collection or null.
         */
        private String trustCertCollectionPath = null;

        /**
         * The authority to check for during certificate checks. By default the clients will use the name of the client
         * to check the server certificate's common + alternative names.
         *
         * @param authorityOverride The authority to check for in the certificate.
         * @return The override for the authority to check for or null, there is no override configured.
         */
        private String authorityOverride = null;

    }

    /**
     * Gets the maximum message size in bytes allowed to be received by the channel. If not set ({@code null}) then it
     * will default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE DEFAULT_MAX_MESSAGE_SIZE}. If set to {@code -1} then it
     * will use the highest possible limit (not recommended).
     *
     * @return The maximum message size allowed or null if the default should be used.
     */
    public Integer getMaxInboundMessageSize() {
        if (this.maxInboundMessageSize != null && this.maxInboundMessageSize == -1) {
            this.maxInboundMessageSize = Integer.MAX_VALUE;
        }
        return this.maxInboundMessageSize;
    }

}

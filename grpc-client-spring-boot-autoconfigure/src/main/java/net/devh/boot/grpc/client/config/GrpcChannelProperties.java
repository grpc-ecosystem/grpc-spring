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

package net.devh.boot.grpc.client.config;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.convert.DurationUnit;

import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverProvider;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The channel properties for a single named gRPC channel or service reference.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @since 5/17/16
 */
@ToString
@EqualsAndHashCode
public class GrpcChannelProperties {

    // --------------------------------------------------
    // Target Address
    // --------------------------------------------------

    private URI address = null;

    /**
     * Gets the target address uri.
     *
     * @return The address to connect to or null
     * @see #setAddress(String)
     */
    public URI getAddress() {
        return this.address;
    }

    /**
     * Set the address uri for the channel. If nothing is configured then the {@link io.grpc.NameResolver.Factory} will
     * decide on the default.
     *
     * @param address The address to use for the channel or null to default to the fallback.
     *
     * @see #setAddress(String)
     */
    public void setAddress(final URI address) {
        this.address = address;
    }

    /**
     * Sets the target address uri for the channel. The target uri must be in the format:
     * {@code schema:[//[authority]][/path]}. If nothing is configured then the {@link io.grpc.NameResolver.Factory}
     * will decide on the default.
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
     * <p>
     * <b>Note:</b> It is possible to use dns based service discovery. The address {@code dns:/example.com} will check
     * the DNS for {@code SVC} entries with the key {@code _grpclb._tcp.example.com}. See also
     * {@link DnsNameResolverProvider}. E.g. in Kubernetes, if you define a service with a port spec called
     * {@code grpclb} and configure the channel address as {@code <service>.<namespace>.svc.cluster.local}, then the
     * DNS-resolver will pickup all available endpoints (including the correct port). Must be enable with:
     * {@code -Dio.grpc.internal.DnsNameResolverProvider.enable_grpclb=true}.
     * </p>
     *
     * @param address The string representation of an uri to use as target address or null to use a fallback.
     *
     * @see <a href="https://github.com/grpc/grpc/blob/master/doc/naming.md">gRPC Name Resolution</a>
     * @see NameResolverProvider
     */
    public void setAddress(final String address) {
        this.address = address == null ? null : URI.create(address);
    }

    // --------------------------------------------------
    // defaultLoadBalancingPolicy
    // --------------------------------------------------

    private String defaultLoadBalancingPolicy;
    private static final String DEFAULT_DEFAULT_LOAD_BALANCING_POLICY = "round_robin";

    /**
     * Gets the default load balancing policy this channel should use.
     *
     * @return The default load balancing policy.
     * @see ManagedChannelBuilder#defaultLoadBalancingPolicy(String)
     */
    public String getDefaultLoadBalancingPolicy() {
        return this.defaultLoadBalancingPolicy == null ? DEFAULT_DEFAULT_LOAD_BALANCING_POLICY
                : this.defaultLoadBalancingPolicy;
    }

    /**
     * Sets the default load balancing policy for this channel. This config might be overwritten by the service config
     * received from the target address. The names have to be resolvable from the {@link LoadBalancerRegistry}. By
     * default this the {@code round_robin} policy. Please note that this policy is different from the normal grpc-java
     * default policy {@code pick_first}.
     *
     * @param defaultLoadBalancingPolicy The default load balancing policy to use or null to use the fallback.
     */
    public void setDefaultLoadBalancingPolicy(final String defaultLoadBalancingPolicy) {
        this.defaultLoadBalancingPolicy = defaultLoadBalancingPolicy;
    }

    // --------------------------------------------------
    // KeepAlive
    // --------------------------------------------------

    private Boolean enableKeepAlive;
    private static final boolean DEFAULT_ENABLE_KEEP_ALIVE = false;

    /**
     * Gets whether keepAlive is enabled.
     *
     * @return True, if keep alive should be enabled. False otherwise.
     *
     * @see #setEnableKeepAlive(Boolean)
     */
    public boolean isEnableKeepAlive() {
        return this.enableKeepAlive == null ? DEFAULT_ENABLE_KEEP_ALIVE : this.enableKeepAlive;
    }

    /**
     * Sets whether keepAlive should be enabled. Defaults to false.
     *
     * @param enableKeepAlive True, to enable. False, to disable. Null, to use the fallback.
     */
    public void setEnableKeepAlive(final Boolean enableKeepAlive) {
        this.enableKeepAlive = enableKeepAlive;
    }

    // --------------------------------------------------

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration keepAliveTime;
    private static final Duration DEFAULT_KEEP_ALIVE_TIME = Duration.of(60, ChronoUnit.SECONDS);

    /**
     * Gets the default delay before we send a keepAlive.
     *
     * @return The default delay before sending keepAlives.
     *
     * @see #setKeepAliveTime(Duration)
     */
    public Duration getKeepAliveTime() {
        return this.keepAliveTime == null ? DEFAULT_KEEP_ALIVE_TIME : this.keepAliveTime;
    }

    /**
     * The default delay before we send a keepAlives. Defaults to {@code 60s}. Default unit {@link ChronoUnit#SECONDS
     * SECONDS}. Please note that shorter intervals increase the network burden for the server.
     *
     * @param keepAliveTime The new default delay before sending keepAlives, or null to use the fallback.
     *
     * @see #setEnableKeepAlive(Boolean)
     * @see NettyServerBuilder#keepAliveTime(long, TimeUnit)
     */
    public void setKeepAliveTime(final Duration keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    // --------------------------------------------------

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration keepAliveTimeout;
    private static final Duration DEFAULT_KEEP_ALIVE_TIMEOUT = Duration.of(20, ChronoUnit.SECONDS);

    /**
     * The default timeout for a keepAlives ping request.
     *
     * @return The default timeout for a keepAlives ping request.
     *
     * @see #setKeepAliveTimeout(Duration)
     */
    public Duration getKeepAliveTimeout() {
        return this.keepAliveTimeout == null ? DEFAULT_KEEP_ALIVE_TIMEOUT : this.keepAliveTimeout;
    }

    /**
     * The default timeout for a keepAlives ping request. Defaults to {@code 20s}. Default unit
     * {@link ChronoUnit#SECONDS SECONDS}.
     *
     * @param keepAliveTimeout The default timeout for a keepAlives ping request.
     *
     * @see #setEnableKeepAlive(Boolean)
     * @see NettyServerBuilder#keepAliveTimeout(long, TimeUnit)
     */
    public void setKeepAliveTimeout(final Duration keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    // --------------------------------------------------

    private Boolean keepAliveWithoutCalls;
    private static final boolean DEFAULT_KEEP_ALIVE_WITHOUT_CALLS = false;

    /**
     * Gets whether keepAlive will be performed when there are no outstanding RPC on a connection.
     *
     * @return True, if keepAlives should be performed even when there are no RPCs. False otherwise.
     *
     * @see #setKeepAliveWithoutCalls(Boolean)
     */
    public boolean isKeepAliveWithoutCalls() {
        return this.keepAliveWithoutCalls == null ? DEFAULT_KEEP_ALIVE_WITHOUT_CALLS : this.keepAliveWithoutCalls;
    }

    /**
     * Sets whether keepAlive will be performed when there are no outstanding RPC on a connection. Defaults to
     * {@code false}.
     *
     * @param keepAliveWithoutCalls whether keepAlive will be performed when there are no outstanding RPC on a
     *        connection.
     *
     * @see #setEnableKeepAlive(Boolean)
     * @see NettyChannelBuilder#keepAliveWithoutCalls(boolean)
     */
    public void setKeepAliveWithoutCalls(final Boolean keepAliveWithoutCalls) {
        this.keepAliveWithoutCalls = keepAliveWithoutCalls;
    }

    // --------------------------------------------------
    // Message Transfer
    // --------------------------------------------------

    private Integer maxInboundMessageSize = null;

    /**
     * Gets the maximum message size in bytes allowed to be received by the channel. If not set ({@code null}) then it
     * {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE gRPC's default} should be used.
     *
     * @return The maximum message size allowed or null if the default should be used.
     *
     * @see #setMaxInboundMessageSize(Integer)
     */
    public Integer getMaxInboundMessageSize() {
        return this.maxInboundMessageSize;
    }

    /**
     * Sets the maximum message size in bytes allowed to be received by the channel. If not set ({@code null}) then it
     * will default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE gRPC's default}. If set to {@code -1} then it will use
     * the highest possible limit (not recommended).
     *
     * @param maxInboundMessageSize The new maximum size in bytes allowed for incoming messages. {@code -1} for max
     *        possible. Null to use the gRPC's default.
     *
     * @see ManagedChannelBuilder#maxInboundMessageSize(int)
     */
    public void setMaxInboundMessageSize(final Integer maxInboundMessageSize) {
        if (maxInboundMessageSize == null || maxInboundMessageSize >= 0) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        } else if (maxInboundMessageSize == -1) {
            this.maxInboundMessageSize = Integer.MAX_VALUE;
        } else {
            throw new IllegalArgumentException("Unsupported maxInboundMessageSize: " + maxInboundMessageSize);
        }
    }

    // --------------------------------------------------

    private Boolean fullStreamDecompression;
    private static final boolean DEFAULT_FULL_STREAM_DECOMPRESSION = false;

    /**
     * Gets whether full-stream decompression of inbound streams should be enabled.
     *
     * @return True, if full-stream decompression of inbound streams should be enabled. False otherwise.
     *
     * @see #setFullStreamDecompression(Boolean)
     */
    public boolean isFullStreamDecompression() {
        return this.fullStreamDecompression == null ? DEFAULT_FULL_STREAM_DECOMPRESSION : this.fullStreamDecompression;
    }

    /**
     * Sets whether full-stream decompression of inbound streams should be enabled. This will cause the channel's
     * outbound headers to advertise support for GZIP compressed streams, and gRPC servers which support the feature may
     * respond with a GZIP compressed stream.
     *
     * @param fullStreamDecompression Whether full stream decompression should be enabled or null to use the fallback.
     *
     * @see ManagedChannelBuilder#enableFullStreamDecompression()
     */
    public void setFullStreamDecompression(final Boolean fullStreamDecompression) {
        this.fullStreamDecompression = fullStreamDecompression;
    }

    // --------------------------------------------------

    private NegotiationType negotiationType;
    private static final NegotiationType DEFAULT_NEGOTIATION_TYPE = NegotiationType.TLS;

    /**
     * Gets the negotiation type to use on the connection.
     *
     * @return The negotiation type that the channel will use.
     *
     * @see #setNegotiationType(NegotiationType)
     */
    public NegotiationType getNegotiationType() {
        return this.negotiationType == null ? DEFAULT_NEGOTIATION_TYPE : this.negotiationType;
    }

    /**
     * Sets the negotiation type to use on the connection. Either of {@link NegotiationType#TLS TLS} (recommended),
     * {@link NegotiationType#PLAINTEXT_UPGRADE PLAINTEXT_UPGRADE} or {@link NegotiationType#PLAINTEXT PLAINTEXT}.
     * Defaults to TLS.
     *
     * @param negotiationType The negotiation type to use or null to use the fallback.
     */
    public void setNegotiationType(final NegotiationType negotiationType) {
        this.negotiationType = negotiationType;
    }

    // --------------------------------------------------

    private final Security security = new Security();

    /**
     * Gets the options for transport security.
     *
     * @return The options for transport security.
     */
    public Security getSecurity() {
        return this.security;
    }

    /**
     * Copies the defaults from the given configuration. Values are considered "default" if they are null. Please note
     * that the getters might return fallback values instead.
     *
     * @param config The config to copy the defaults from.
     */
    public void copyDefaultsFrom(final GrpcChannelProperties config) {
        if (this == config) {
            return;
        }
        if (this.address == null) {
            this.address = config.address;
        }
        if (this.defaultLoadBalancingPolicy == null) {
            this.defaultLoadBalancingPolicy = config.defaultLoadBalancingPolicy;
        }
        if (this.enableKeepAlive == null) {
            this.enableKeepAlive = config.enableKeepAlive;
        }
        if (this.keepAliveTime == null) {
            this.keepAliveTime = config.keepAliveTime;
        }
        if (this.keepAliveTimeout == null) {
            this.keepAliveTimeout = config.keepAliveTimeout;
        }
        if (this.keepAliveWithoutCalls == null) {
            this.keepAliveWithoutCalls = config.keepAliveWithoutCalls;
        }
        if (this.maxInboundMessageSize == null) {
            this.maxInboundMessageSize = config.maxInboundMessageSize;
        }
        if (this.fullStreamDecompression == null) {
            this.fullStreamDecompression = config.fullStreamDecompression;
        }
        if (this.negotiationType == null) {
            this.negotiationType = config.negotiationType;
        }
        this.security.copyDefaultsFrom(config.security);
    }

    /**
     * A container with options for the channel's transport security.
     */
    @ToString
    @EqualsAndHashCode
    public static class Security {

        private Boolean clientAuthEnabled;
        private static final boolean DEFAULT_CLIENT_AUTH_ENABLED = false;

        /**
         * Gets whether client can authenticate using certificates.
         *
         * @return True, if the client can authenticate itself using certificates.
         *
         * @see #setClientAuthEnabled(Boolean)
         */
        public boolean isClientAuthEnabled() {
            return this.clientAuthEnabled == null ? DEFAULT_CLIENT_AUTH_ENABLED : this.clientAuthEnabled;
        }

        /**
         * Set whether client can authenticate using certificates. Defaults to {@code false}.
         *
         * @param clientAuthEnabled Whether the client can authenticate itself using certificates.
         */
        public void setClientAuthEnabled(final Boolean clientAuthEnabled) {
            this.clientAuthEnabled = clientAuthEnabled;
        }

        // --------------------------------------------------

        private String certificateChainPath = null;

        /**
         * Gets the path to SSL certificate chain.
         *
         * @return The path to the certificate chain or null, if security is not enabled.
         * @see #setCertificateChainPath(String)
         */
        public String getCertificateChainPath() {
            return this.certificateChainPath;
        }

        /**
         * Sets the path to SSL certificate chain. Required if {@link #isClientAuthEnabled()} is true. The linked
         * certificate will be used to authenticate the client.
         *
         * @param certificateChainPath The path to the certificate chain.
         *
         * @see SslContextBuilder#keyManager(File, File)
         */
        public void setCertificateChainPath(final String certificateChainPath) {
            this.certificateChainPath = certificateChainPath;
        }

        // --------------------------------------------------

        private String privateKeyPath = null;

        /**
         * Gets the path to the private key.
         *
         * @return The path to the private key or null, if security is not enabled.
         *
         * @see #setPrivateKeyPath(String)
         */
        public String getPrivateKeyPath() {
            return this.privateKeyPath;
        }

        /**
         * Sets the path to the private key. Required if {@link #isClientAuthEnabled} is true.
         *
         * @param privateKeyPath The path to the private key.
         *
         * @see SslContextBuilder#keyManager(File, File)
         */
        public void setPrivateKeyPath(final String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }

        // --------------------------------------------------

        private String trustCertCollectionPath = null;

        /**
         * Gets the path to the trusted certificate collection. If {@code null} or empty the use the system's default
         * collection should be used.
         *
         * @return The path to the trusted certificate collection or null.
         *
         * @see #setTrustCertCollectionPath(String)
         */
        public String getTrustCertCollectionPath() {
            return this.trustCertCollectionPath;
        }

        /**
         * Sets the path to the trusted certificate collection. If not set ({@code null}) it will use the system's
         * default collection (Default). This collection will be used to verify server certificates.
         *
         * @param trustCertCollectionPath The path to the trusted certificate collection.
         *
         * @see SslContextBuilder#trustManager(File)
         */
        public void setTrustCertCollectionPath(final String trustCertCollectionPath) {
            this.trustCertCollectionPath = trustCertCollectionPath;
        }

        // --------------------------------------------------

        private String authorityOverride = null;

        /**
         * Gets the authority to check for during server certificate verification.
         *
         * @return The override for the authority to check for or null, there is no override configured.
         *
         * @see #setAuthorityOverride(String)
         */
        public String getAuthorityOverride() {
            return this.authorityOverride;
        }

        /**
         * Sets the authority to check for during server certificate verification. By default the clients will use the
         * name of the client to check the server certificate's common + alternative names.
         *
         * @param authorityOverride The authority to check for in the certificate, or null to use the default checks.
         *
         * @see NettyChannelBuilder#overrideAuthority(String)
         */
        public void setAuthorityOverride(final String authorityOverride) {
            this.authorityOverride = authorityOverride;
        }

        // --------------------------------------------------

        private String ciphers = null;

        /**
         * @return The cipher suite accepted for secure connections or null.
         */
        public String getCiphers() {
            return ciphers;
        }

        /**
         * @param ciphers Cipher suite consisting of one or more cipher strings separated by colons, commas or spaces
         */
        public void setCiphers(String ciphers) {
            this.ciphers = ciphers;
        }

        // --------------------------------------------------

        private String protocols = null;

        /**
         * @return The protocols accepted for secure connections or null.
         */
        public String getProtocols() {
            return protocols;
        }

        /**
         * @param protocols Protocol list consisting of one or more protocols separated by colons, commas or spaces.
         */
        public void setProtocols(String protocols) {
            this.protocols = protocols;
        }

        // --------------------------------------------------

        /**
         * Copies the defaults from the given configuration. Values are considered "default" if they are null. Please
         * note that the getters might return fallback values instead.
         *
         * @param config The config to copy the defaults from.
         */
        public void copyDefaultsFrom(final Security config) {
            if (this == config) {
                return;
            }
            if (this.clientAuthEnabled == null) {
                this.clientAuthEnabled = config.clientAuthEnabled;
            }
            if (this.certificateChainPath == null) {
                this.certificateChainPath = config.certificateChainPath;
            }
            if (this.privateKeyPath == null) {
                this.privateKeyPath = config.privateKeyPath;
            }
            if (this.trustCertCollectionPath == null) {
                this.trustCertCollectionPath = config.trustCertCollectionPath;
            }
            if (this.authorityOverride == null) {
                this.authorityOverride = config.authorityOverride;
            }
            if(this.ciphers == null) {
                this.ciphers = config.ciphers;
            }
            if(this.protocols == null) {
                this.protocols = config.protocols;
            }
        }

    }

}

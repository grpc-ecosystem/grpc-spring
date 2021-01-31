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

package net.devh.boot.grpc.server.config;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.core.io.Resource;
import org.springframework.util.SocketUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import io.grpc.ServerBuilder;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import lombok.Data;

/**
 * The properties for the gRPC server that will be started as part of the application.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Data
@ConfigurationProperties("grpc.server")
@SuppressWarnings("javadoc")
public class GrpcServerProperties {

    /**
     * A constant that defines, that the server should listen to any IPv4 and IPv6 address.
     */
    public static final String ANY_IP_ADDRESS = "*";

    /**
     * A constant that defines, that the server should listen to any IPv4 address.
     */
    public static final String ANY_IPv4_ADDRESS = "0.0.0.0";

    /**
     * A constant that defines, that the server should listen to any IPv6 address.
     */
    public static final String ANY_IPv6_ADDRESS = "::";

    /**
     * Bind address for the server. Defaults to {@link #ANY_IP_ADDRESS "*"}. Alternatively you can restrict this to
     * {@link #ANY_IPv4_ADDRESS "0.0.0.0"} or {@link #ANY_IPv6_ADDRESS "::"}. Or restrict it to exactly one IP address.
     *
     * @param address The address to bind to.
     * @return The address the server should bind to.
     */
    private String address = ANY_IP_ADDRESS;

    /**
     * Server port to listen on. Defaults to {@code 9090}. If set to {@code 0} a random available port will be selected
     * and used. Use {@code -1} to disable the inter-process server (for example if you only want to use the in-process
     * server).
     *
     * @param port The port the server should listen on.
     * @return The port the server will listen on.
     */
    private int port = 9090;

    /**
     * The name of the in-process server. If not set, then the in process server won't be started.
     *
     * @param inProcessName The name of the in-process server.
     * @return The name of the in-process server or null if isn't configured.
     */
    private String inProcessName;

    /**
     * The time to wait for the server to gracefully shutdown (completing all requests after the server started to
     * shutdown). If set to {@code -1} the server waits forever. If set to {@code 0} the server will force shutdown
     * immediately. Defaults to {@code 30s}.
     * 
     * @param gracefullShutdownTimeout The time to wait for a graceful shutdown.
     * @return The time to wait for a graceful shutdown.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration shutdownGracePeriod = Duration.of(30, ChronoUnit.SECONDS);

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
     * Specify the most aggressive keep-alive time clients are permitted to configure. Defaults to {@code 5min}. Default
     * unit {@link ChronoUnit#SECONDS SECONDS}.
     *
     * @see NettyServerBuilder#permitKeepAliveTime(long, TimeUnit)
     *
     * @param permitKeepAliveTime The most aggressive keep-alive time clients are permitted to configure.
     * @return The most aggressive keep-alive time clients are permitted to configure.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration permitKeepAliveTime = Duration.of(5, ChronoUnit.MINUTES);

    /**
     * Whether clients are allowed to send keep-alive HTTP/2 PINGs even if there are no outstanding RPCs on the
     * connection. Defaults to {@code false}.
     *
     * @see NettyServerBuilder#permitKeepAliveWithoutCalls(boolean)
     *
     * @param permitKeepAliveWithoutCalls Whether to allow clients to send keep-alive requests without calls.
     * @return True, if clients are allowed to send keep-alive requests without calls. False otherwise.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private boolean permitKeepAliveWithoutCalls = false;

    /**
     * The maximum message size allowed to be received by the server. If not set ({@code null}) then
     * {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE gRPC's default} should be used.
     *
     * @return The maximum message size allowed.
     */
    @DataSizeUnit(DataUnit.BYTES)
    private DataSize maxInboundMessageSize = null;

    /**
     * The maximum size of metadata allowed to be received. If not set ({@code null}) then
     * {@link GrpcUtil#DEFAULT_MAX_HEADER_LIST_SIZE gRPC's default} should be used.
     * 
     * @return The maximum metadata size allowed.
     */
    @DataSizeUnit(DataUnit.BYTES)
    private DataSize maxInboundMetadataSize = null;

    /**
     * Whether gRPC health service is enabled or not. Defaults to {@code true}.
     *
     * @param healthServiceEnabled Whether gRPC health service is enabled.
     * @return True, if the health service is enabled. False otherwise.
     */
    private boolean healthServiceEnabled = true;

    /**
     * Whether proto reflection service is enabled or not. Defaults to {@code true}.
     *
     * @param reflectionServiceEnabled Whether gRPC reflection service is enabled.
     * @return True, if the reflection service is enabled. False otherwise.
     */
    private boolean reflectionServiceEnabled = true;

    /**
     * Security options for transport security. Defaults to disabled. We strongly recommend to enable this though.
     *
     * @return The security options for transport security.
     */
    private final Security security = new Security();

    /**
     * The security configuration for the gRPC server.
     */
    @Data
    public static class Security {

        /**
         * Flag that controls whether transport security is used. Defaults to {@code false}.
         *
         * @param enabled Whether transport security should be enabled.
         * @return True, if transport security should be enabled. False otherwise.
         */
        private boolean enabled = false;

        /**
         * The resource containing the SSL certificate chain. Required if {@link #isEnabled()} is true.
         *
         * @see GrpcSslContexts#forServer(InputStream, InputStream, String)
         *
         * @param certificateChain The certificate chain resource.
         * @return The certificate chain resource or null, if security is not enabled.
         */
        private Resource certificateChain = null;

        /**
         * The resource containing the private key. Required if {@link #enabled} is true.
         *
         * @see GrpcSslContexts#forServer(InputStream, InputStream, String)
         *
         * @param privateKey The private key resource.
         * @return The private key resource or null, if security is not enabled.
         */
        private Resource privateKey = null;

        /**
         * Password for the private key.
         *
         * @see GrpcSslContexts#forServer(File, File, String)
         *
         * @param privateKeyPassword The password for the private key.
         * @return The password for the private key or null, if the private key is not set or not encrypted.
         */
        private String privateKeyPassword = null;

        /**
         * Whether the client has to authenticate himself via certificates. Can be either of {@link ClientAuth#NONE
         * NONE}, {@link ClientAuth#OPTIONAL OPTIONAL} or {@link ClientAuth#REQUIRE REQUIRE}. Defaults to
         * {@link ClientAuth#NONE}.
         *
         * @see SslContextBuilder#clientAuth(io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth)
         *      SslContextBuilder#clientAuth(ClientAuth)
         *
         * @param clientAuth Whether the client has to authenticate himself via certificates.
         * @return Whether the client has to authenticate himself via certificates.
         */
        private ClientAuth clientAuth = ClientAuth.NONE;

        /**
         * The resource containing the trusted certificate collection. If {@code null} or empty it will use the system's
         * default collection (Default). This collection will be used to verify client certificates.
         *
         * @see SslContextBuilder#trustManager(InputStream)
         *
         * @param trustCertCollection The trusted certificate collection resource.
         * @return The trusted certificate collection resource or null.
         */
        private Resource trustCertCollection = null;

        /**
         * Specifies the cipher suite. If {@code null} or empty it will use the system's default cipher suite.
         *
         * @param ciphers List of allowed ciphers
         * @return The cipher suite accepted for secure connections or null.
         */
        private List<String> ciphers = null;

        public void setCiphers(final String ciphers) {
            this.ciphers = Arrays.asList(ciphers.split("[ :,]"));
        }

        /**
         * Specifies the protocols accepted for secure connections. If {@code null} or empty it will use the system's
         * default (all supported) protocols.
         *
         * @param protocols Protocol list consisting of one or more protocols separated by colons, commas or spaces.
         * @return The protocols accepted for secure connections or null.
         */
        private String[] protocols = null;

        public void setProtocols(final String protocols) {
            this.protocols = protocols.split("[ :,]");
        }

    }

    /**
     * Gets the port the server should listen on. Defaults to {@code 9090}. If set to {@code 0} a random available port
     * will be selected and used.
     *
     * @return The server port to listen to.
     *
     * @see #setPort(int)
     */
    public int getPort() {
        if (this.port == 0) {
            this.port = SocketUtils.findAvailableTcpPort();
        }
        return this.port;
    }

    /**
     * Sets the maximum message size allowed to be received by the server. If not set ({@code null}) then it will
     * default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE gRPC's default}. If set to {@code -1} then it will use the
     * highest possible limit (not recommended).
     *
     * @param maxInboundMessageSize The new maximum size allowed for incoming messages. {@code -1} for max possible.
     *        Null to use the gRPC's default.
     *
     * @see ServerBuilder#maxInboundMessageSize(int)
     */
    public void setMaxInboundMessageSize(final DataSize maxInboundMessageSize) {
        if (maxInboundMessageSize == null || maxInboundMessageSize.toBytes() >= 0) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        } else if (maxInboundMessageSize.toBytes() == -1) {
            this.maxInboundMessageSize = DataSize.ofBytes(Integer.MAX_VALUE);
        } else {
            throw new IllegalArgumentException("Unsupported maxInboundMessageSize: " + maxInboundMessageSize);
        }
    }

    /**
     * Sets the maximum metadata size allowed to be received by the server. If not set ({@code null}) then it will
     * default to {@link GrpcUtil#DEFAULT_MAX_HEADER_LIST_SIZE gRPC's default}. If set to {@code -1} then it will use
     * the highest possible limit (not recommended).
     *
     * @param maxInboundMetadataSize The new maximum size allowed for incoming metadata. {@code -1} for max possible.
     *        Null to use the gRPC's default.
     *
     * @see ServerBuilder#maxInboundMetadataSize(int)
     */
    public void setMaxInboundMetadataSize(final DataSize maxInboundMetadataSize) {
        if (maxInboundMetadataSize == null || maxInboundMetadataSize.toBytes() >= 0) {
            this.maxInboundMetadataSize = maxInboundMetadataSize;
        } else if (maxInboundMetadataSize.toBytes() == -1) {
            this.maxInboundMetadataSize = DataSize.ofBytes(Integer.MAX_VALUE);
        } else {
            throw new IllegalArgumentException("Unsupported maxInboundMetadataSize: " + maxInboundMetadataSize);
        }
    }

}

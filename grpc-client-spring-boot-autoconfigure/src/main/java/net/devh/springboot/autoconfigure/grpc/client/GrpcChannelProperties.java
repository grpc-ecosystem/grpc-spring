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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NegotiationType;
import lombok.Data;

/**
 * The channel properties for a single named grpc channel or service reference.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Data
public class GrpcChannelProperties {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final Integer DEFAULT_PORT = 9090;
    private static final List<String> DEFAULT_HOSTS = Arrays.asList(DEFAULT_HOST);
    private static final List<Integer> DEFAULT_PORTS = Arrays.asList(DEFAULT_PORT);

    public static final GrpcChannelProperties DEFAULT = new GrpcChannelProperties();

    /**
     * A list of hosts to connect to. These entries should be kept in tandem with the port entries. Defaults to
     * {@link #DEFAULT_HOST}.
     */
    private List<String> host = new ArrayList<>(DEFAULT_HOSTS);

    /**
     * A list of ports to connect to. These entries should be kept in tandem with the host entries. Defaults to
     * {@link #DEFAULT_PORT}.
     */
    private List<Integer> port = new ArrayList<>(DEFAULT_PORTS);

    /**
     * Setting to enable keepalive. Default to {@code false}.
     */
    private boolean enableKeepAlive = false;

    /**
     * Sets whether keepalive will be performed when there are no outstanding RPC on a connection. Defaults to
     * {@code false}.
     */
    private boolean keepAliveWithoutCalls = false;

    /**
     * The default delay in seconds before we send a keepalive. Defaults to {@code 60}.
     */
    private long keepAliveTime = 60;

    /**
     * The default timeout in seconds for a keepalive ping request. Defaults to {@code 20}.
     */
    private long keepAliveTimeout = 20;

    /**
     * The maximum message size in bytes allowed to be received by the channel. If not set ({@code null}) then it will
     * default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE DEFAULT_MAX_MESSAGE_SIZE}. If set to {@code -1} then it will
     * use {@link Integer#MAX_VALUE} as limit.
     */
    private Integer maxInboundMessageSize = null;

    private boolean fullStreamDecompression = false;

    /**
     * The negotiation type to use on the connection. Either of {@link NegotiationType#TLS TLS} (recommended),
     * {@link NegotiationType#PLAINTEXT_UPGRADE PLAINTEXT_UPGRADE} or {@link NegotiationType#PLAINTEXT PLAINTEXT}.
     * Defaults to TLS.
     */
    private NegotiationType negotiationType = NegotiationType.TLS;

    /**
     * Security options for transport security.
     */
    private final Security security = new Security();

    @Data
    public static class Security {

        /**
         * Flag that controls whether client can authenticate using certificates. Defaults to {@code false}.
         */
        private boolean clientAuthEnabled = false;

        /**
         * Path to SSL certificate chain. Required if {@link #clientAuthEnabled} is true.
         */
        private String certificateChainPath = null;

        /**
         * Path to private key. Required if {@link #clientAuthEnabled} is true.
         */
        private String privateKeyPath = null;

        /**
         * Path to the trusted certificate collection. If {@code null} or empty it will use the system's default
         * collection (Default).
         */
        private String trustCertCollectionPath = null;

        /**
         * The authority to check for during certificate checks. By default the clients will use the name of the client
         * to check the server certificate's common + alternative names.
         */
        private String authorityOverride = null;

    }

    /**
     * Gets the maximum message size in bytes allowed to be received by the channel. If not set ({@code null}) then it
     * will default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE DEFAULT_MAX_MESSAGE_SIZE}. If set to {@code -1} then it
     * will use {@link Integer#MAX_VALUE} as limit.
     *
     * @return The maximum message size in bytes allowed or null if the default should be used.
     */
    public Integer getMaxInboundMessageSize() {
        if (this.maxInboundMessageSize != null && this.maxInboundMessageSize == -1) {
            this.maxInboundMessageSize = Integer.MAX_VALUE;
        }
        return this.maxInboundMessageSize;
    }

}

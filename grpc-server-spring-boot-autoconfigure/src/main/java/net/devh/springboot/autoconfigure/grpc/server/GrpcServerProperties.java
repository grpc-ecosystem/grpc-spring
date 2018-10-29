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

package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.SocketUtils;

import io.grpc.internal.GrpcUtil;
import io.netty.handler.ssl.ClientAuth;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * The properties for the grpc server that will be started as part of the application.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Data
@Slf4j
@ConfigurationProperties("grpc.server")
public class GrpcServerProperties {

    /**
     * Bind address for the server. Defaults to {@code 0.0.0.0}.
     */
    private String address = "0.0.0.0";

    /**
     * Server port to listen on. Defaults to {@code 9090}. If set to {@code 0} a random available port will be selected
     * and used.
     */
    private int port = 9090;

    /**
     * The maximum message size in bytes allowed to be received by the server. If not set ({@code null}) then it will
     * default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE DEFAULT_MAX_MESSAGE_SIZE}. If set to {@code -1} then it will
     * use {@link Integer#MAX_VALUE} as limit.
     */
    private Integer maxInboundMessageSize = null;

    /**
     * Whether grpc health service is enabled or not. Defaults to {@code true}.
     */
    private boolean healthServiceEnabled = true;

    /**
     * Whether proto reflection service is enabled or not. Defaults to {@code true}.
     */
    private boolean reflectionServiceEnabled = true;

    /**
     * Security options for transport security. Defaults to disabled.
     */
    private final Security security = new Security();

    @Data
    public static class Security {

        /**
         * Flag that controls whether transport security is used. Defaults to {@code false}.
         */
        private boolean enabled = false;

        /**
         * Path to SSL certificate chain. Required if {@link #enabled} is true.
         */
        private String certificateChainPath = null;

        /**
         * Path to private key. Required if {@link #enabled} is true.
         */
        private String privateKeyPath = null;

        /**
         * Whether the client has to authenticate himself via certificates. Can be either of {@link ClientAuth#NONE
         * NONE}, {@link ClientAuth#OPTIONAL OPTIONAL} or {@link ClientAuth#REQUIRE REQUIRE}. Defaults to
         * {@link ClientAuth#NONE}.
         */
        private ClientAuth clientAuth = ClientAuth.NONE;

        /**
         * Path to the trusted certificate collection. If {@code null} or empty it will use the system's default
         * collection (Default).
         */
        private String trustCertCollectionPath = null;

        @Deprecated
        public void setCertificatePath(final String certificatePath) {
            log.warn("The 'grpc.server.security.certificatePath' property is deprecated. "
                    + "Use 'grpc.server.security.privateKeyPath' instead!");
            setPrivateKeyPath(certificatePath);
        }

    }

    /**
     * Gets the port the server should listen on. Defaults to {@code 9090}. If set to {@code 0} a random available port
     * will be selected and used.
     *
     * @return The server port to listen to.
     */
    public int getPort() {
        if (this.port == 0) {
            this.port = SocketUtils.findAvailableTcpPort();
        }
        return this.port;
    }

    @Deprecated
    public void setMaxMessageSize(final int maxMessageSize) {
        log.warn("The 'grpc.server.maxMessageSize' property is deprecated. "
                + "Use 'grpc.server.maxInboundMessageSize' instead!");
        this.maxInboundMessageSize = maxMessageSize == 0 ? null : maxMessageSize;
    }

    /**
     * Gets the maximum message size in bytes allowed to be received by the server. If not set ({@code null}) then it
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

package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.SocketUtils;

import lombok.Data;

/**
 * The properties for the grpc server that will be started as part of the application.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Data
@ConfigurationProperties("grpc.server")
public class GrpcServerProperties {

    /**
     * Bind address for the server. Defaults to {@code 0.0.0.0}.
     */
    private String address = "0.0.0.0";

    /**
     * Server port to listen on. Defaults to {@code 9090}. If set to {@code 0} a random available port
     * will be used.
     */
    private int port = 9090;

    /**
     * The maximum message size allowed to be received for the server.
     */
    private int maxMessageSize;

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
         * Path to SSL certificate chain.
         */
        private String certificateChainPath = "";

        /**
         * Path to SSL certificate.
         */
        private String certificatePath = "";

    }

    /**
     * Gets the port the server should listen on. Defaults to {@code 9090}. If set to {@code 0} a random
     * available port will be used.
     *
     * @return The server port to listen to.
     */
    public int getPort() {
        if (this.port == 0) {
            this.port = SocketUtils.findAvailableTcpPort();
        }
        return this.port;
    }

}

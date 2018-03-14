package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Data
@ConfigurationProperties("grpc.server")
public class GrpcServerProperties {
    /**
     * Server port to listen on. Defaults to 9090.
     */
    private int port = 9090;

    /**
     * Bind address for the server. Defaults to 0.0.0.0.
     */
    private String address = "0.0.0.0";
    
    /**
     * The maximum message size allowed to be received for the server.
     */
    private int maxMessageSize;

    /**
     * Security options for transport security. Defaults to disabled. 
     */
    private final Security security = new Security();

    @Data
    public static class Security {

        /**
         * Flag that controls whether transport security is used
         */
        private Boolean enabled = false;

         /**
         * Path to SSL certificate chain
         */
        private String certificateChainPath = "";

         /**
         * Path to SSL certificate
         */
        private String certificatePath = "";

    }

}

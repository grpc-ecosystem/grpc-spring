package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
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
     * Security options for transport security. Defaults to disabled. 
     */
    private final Security security = new Security();

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

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getCertificateChainPath() {
            return certificateChainPath;
        }

        public void setCertificateChainPath(String certificateChainPath) {
            this.certificateChainPath = certificateChainPath;
        }

        public String getCertificatePath() {
            return certificatePath;
        }

        public void setCertificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Security getSecurity() {
        return security;
    }
}

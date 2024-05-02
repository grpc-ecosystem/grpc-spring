package net.devh.boot.grpc.server.config;

/**
 * Enum to specify the type of health service to use in GRPC.
 */
public enum HealthType {
    /**
     * Use the standard GRPC health service from io.grpc.
     */
    GRPC,
    /**
     * Uses a bridge to the Spring Boot Actuator health service.
     */
    ACTUATOR
}

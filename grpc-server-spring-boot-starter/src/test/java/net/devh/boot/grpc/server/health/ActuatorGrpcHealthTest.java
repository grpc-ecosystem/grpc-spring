package net.devh.boot.grpc.server.health;

import io.grpc.StatusException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActuatorGrpcHealthTest {
    @Mock
    HealthEndpoint healthEndpoint;

    ActuatorGrpcHealth server;

    @BeforeEach
    public void setup() {
        this.server = new ActuatorGrpcHealth(healthEndpoint);
    }


    @Test
    void testDefaultServiceWorking() {
        StreamRecorder<HealthCheckResponse> response = StreamRecorder.create();
        HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("").build();

        HealthComponent healthResult = Health.up().build();

        when(healthEndpoint.health())
                .thenReturn(healthResult);

        server.check(request, response);

        List<HealthCheckResponse> result = response.getValues();
        assertEquals(1, result.size());
        assertEquals(HealthCheckResponse.ServingStatus.SERVING, result.get(0).getStatus());
    }

    @Test
    void testDefaultServiceNotWorking() {
        StreamRecorder<HealthCheckResponse> response = StreamRecorder.create();
        HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("").build();

        HealthComponent healthResult = Health.down().build();

        when(healthEndpoint.health())
                .thenReturn(healthResult);

        server.check(request, response);

        List<HealthCheckResponse> result = response.getValues();

        assertEquals(1, result.size());

        assertEquals(HealthCheckResponse.ServingStatus.NOT_SERVING, result.get(0).getStatus());
    }

    @Test
    void testSpecificServiceNotFound() {
        StreamRecorder<HealthCheckResponse> response = StreamRecorder.create();
        HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("someunknownservice").build();

        when(healthEndpoint.healthForPath("someunknownservice"))
                .thenReturn(null);

        server.check(request, response);

        assertEquals(0, response.getValues().size());

        var error = response.getError();
        assertNotNull(error);
        assertInstanceOf(StatusException.class, error);
        
        var statusException = (StatusException) error;
        assertEquals(io.grpc.Status.NOT_FOUND.getCode(), statusException.getStatus().getCode());
    }

    @Test
    void testSpecificServiceUp() {
        StreamRecorder<HealthCheckResponse> response = StreamRecorder.create();
        HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("db").build();

        HealthComponent healthResult = Health.up().build();
        when(healthEndpoint.healthForPath("db"))
                .thenReturn(healthResult);

        server.check(request, response);

        List<HealthCheckResponse> result = response.getValues();
        assertEquals(1, result.size());
        assertEquals(HealthCheckResponse.ServingStatus.SERVING, result.get(0).getStatus());
    }

}
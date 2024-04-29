package net.devh.boot.grpc.server.health;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.actuate.health.HealthEndpoint;

import java.util.Objects;

public class ActuatorGrpcHealth extends HealthGrpc.HealthImplBase {
    private final HealthEndpoint healthEndpoint;

    public ActuatorGrpcHealth(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {

        if (!request.getService().isEmpty()) {
            var health = healthEndpoint.healthForPath(request.getService());
            if(health == null) {
                responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("unknown service " + request.getService())));
                return;
            }
            var status = health.getStatus();
            HealthCheckResponse.ServingStatus result = resolveStatus(status);
            HealthCheckResponse response = HealthCheckResponse.newBuilder().setStatus(result).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {

            var status = healthEndpoint.health().getStatus();
            HealthCheckResponse.ServingStatus result = resolveStatus(status);
            HealthCheckResponse response = HealthCheckResponse.newBuilder().setStatus(result).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    }

    private HealthCheckResponse.ServingStatus resolveStatus(org.springframework.boot.actuate.health.Status status) {
        if (Objects.equals(org.springframework.boot.actuate.health.Status.UP.getCode(), status.getCode())) {
            return HealthCheckResponse.ServingStatus.SERVING;
        }
        if (Objects.equals(org.springframework.boot.actuate.health.Status.DOWN.getCode(), status.getCode()) || Objects.equals(org.springframework.boot.actuate.health.Status.OUT_OF_SERVICE.getCode(), status.getCode())) {
            return HealthCheckResponse.ServingStatus.NOT_SERVING;
        }
        return HealthCheckResponse.ServingStatus.UNKNOWN;
    }
}

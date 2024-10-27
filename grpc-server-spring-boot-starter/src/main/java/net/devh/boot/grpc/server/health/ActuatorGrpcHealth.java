/*
 * Copyright (c) 2016-2024 The gRPC-Spring Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.devh.boot.grpc.server.health;


import static io.grpc.Status.NOT_FOUND;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;

import io.grpc.StatusException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;

public class ActuatorGrpcHealth extends HealthGrpc.HealthImplBase {
    private final HealthEndpoint healthEndpoint;

    public ActuatorGrpcHealth(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {

        if (!request.getService().isEmpty()) {
            HealthComponent health = healthEndpoint.healthForPath(request.getService());
            if (health == null) {
                responseObserver.onError(
                        new StatusException(NOT_FOUND.withDescription("unknown service " + request.getService())));
                return;
            }
            Status status = health.getStatus();
            HealthCheckResponse.ServingStatus result = resolveStatus(status);
            HealthCheckResponse response = HealthCheckResponse.newBuilder().setStatus(result).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            Status status = healthEndpoint.health().getStatus();
            HealthCheckResponse.ServingStatus result = resolveStatus(status);
            HealthCheckResponse response = HealthCheckResponse.newBuilder().setStatus(result).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    }

    private HealthCheckResponse.ServingStatus resolveStatus(Status status) {
        if (Status.UP.equals(status)) {
            return HealthCheckResponse.ServingStatus.SERVING;
        }
        if (Status.DOWN.equals(status) || Status.OUT_OF_SERVICE.equals(status)) {
            return HealthCheckResponse.ServingStatus.NOT_SERVING;
        }
        return HealthCheckResponse.ServingStatus.UNKNOWN;
    }
}

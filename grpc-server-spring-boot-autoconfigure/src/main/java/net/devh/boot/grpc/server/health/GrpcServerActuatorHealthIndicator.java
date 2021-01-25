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

package net.devh.boot.grpc.server.health;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import io.grpc.Channel;
import io.grpc.Deadline;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import reactor.core.publisher.Mono;

/**
 * This class will expose the status of the server into the actuator when it has the "show-components" configuration
 * set. To retrieve the status the health probe actually hits the server and maps the status
 */
public class GrpcServerActuatorHealthIndicator extends AbstractReactiveHealthIndicator {

    private final static String STATUS = "status";
    private final static String DOWN_STATUS = "DOWN";
    private HealthGrpc.HealthFutureStub healthCheckClient;
    private long deadlineMs = 500;

    public GrpcServerActuatorHealthIndicator(String host, int port, long deadline) {
        String internalHost = host.equals("*") ? "0.0.0.0" : host;
        Channel channel = ManagedChannelBuilder
                .forAddress(internalHost, port)
                .disableRetry()
                .build();
        healthCheckClient = HealthGrpc.newFutureStub(channel);
        if (deadline > 0) {
            this.deadlineMs = deadline;
        }
    }

    @Override
    protected Mono<Health> doHealthCheck(Health.Builder builder) {
        ListenableFuture<HealthCheckResponse> responseFuture = healthCheckClient
                .withDeadline(Deadline.after(deadlineMs, TimeUnit.MILLISECONDS))
                .check(HealthCheckRequest.newBuilder().build());
        Mono<Health> ret = Mono.fromFuture(toCompletableFuture(responseFuture))
                .map(response -> {
                    HealthCheckResponse.ServingStatus status = response.getStatus();
                    if (HealthCheckResponse.ServingStatus.SERVING.equals(status)) {
                        builder.withDetail(STATUS, status.toString()).status(Status.UP);
                    } else if (HealthCheckResponse.ServingStatus.NOT_SERVING.equals(status)) {
                        builder.withDetail(STATUS, status.toString()).status(Status.OUT_OF_SERVICE);
                    } else {
                        builder.withDetail(STATUS, status.toString()).status(Status.UNKNOWN);
                    }
                    return builder.build();
                })
                .onErrorReturn(builder.withDetail(STATUS, DOWN_STATUS).status(Status.DOWN).build());
        return ret;
    }

    private <V> CompletableFuture<V> toCompletableFuture(ListenableFuture<V> listenableFuture) {
        CompletableFuture<V> ret = new CompletableFuture<V>();
        Futures.addCallback(listenableFuture, new FutureCallback<V>() {
            @Override
            public void onSuccess(@Nullable V result) {
                ret.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                ret.completeExceptionally(t);
            }
        }, MoreExecutors.directExecutor());
        return ret;
    }
}

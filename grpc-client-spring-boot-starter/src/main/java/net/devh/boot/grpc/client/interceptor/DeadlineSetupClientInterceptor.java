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

package net.devh.boot.grpc.client.interceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Deadline setup client interceptor that create new deadline instance from deadlineDuration.
 *
 * @author Sergei Batsura (batsura.sa@gmail.com)
 */
@Slf4j
@GrpcGlobalClientInterceptor
@RequiredArgsConstructor
public class DeadlineSetupClientInterceptor implements ClientInterceptor {

    private final CallOptions.Key<Duration> deadlineDuration;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            final MethodDescriptor<ReqT, RespT> method,
            final CallOptions callOptions,
            final Channel next) {

        Duration duration = callOptions.getOption(deadlineDuration);
        if (duration != null) {
            return next.newCall(method, callOptions.withDeadlineAfter(duration.toMillis(), TimeUnit.MILLISECONDS));
        } else {
            return next.newCall(method, callOptions);
        }
    }
}

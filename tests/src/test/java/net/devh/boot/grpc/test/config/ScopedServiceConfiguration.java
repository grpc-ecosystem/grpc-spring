/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
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

package net.devh.boot.grpc.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import net.devh.boot.grpc.server.scope.GrpcRequestScope;
import net.devh.boot.grpc.test.server.ScopedTestServiceImpl;
import net.devh.boot.grpc.test.server.ScopedTestServiceImpl.RequestId;
import net.devh.boot.grpc.test.server.TestServiceImpl;

@Configuration
public class ScopedServiceConfiguration extends ServiceConfiguration {

    @Override
    @Bean
    TestServiceImpl testService() {
        return new ScopedTestServiceImpl();
    }

    @Bean
    @Scope(scopeName = GrpcRequestScope.GRPC_REQUEST_SCOPE_NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
    RequestId requestId() {
        return new RequestId();
    }

}

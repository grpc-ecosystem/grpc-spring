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

import static net.devh.boot.grpc.server.security.check.AccessPredicate.SocketPredicate.inProcess;
import static net.devh.boot.grpc.server.security.check.AccessPredicate.SocketPredicate.inet;
import static net.devh.boot.grpc.server.security.check.AccessPredicate.fromClientAddress;
import static net.devh.boot.grpc.server.security.check.AccessPredicate.hasRole;
import static net.devh.boot.grpc.server.security.check.AccessPredicate.permitAll;
import static net.devh.boot.grpc.server.security.check.AccessPredicate.toServerAddress;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;

import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

@Configuration
public class ManualSecurityConfiguration {

    @Bean
    AccessDecisionManager accessDecisionManager() {
        final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        voters.add(new AccessPredicateVoter());
        return new UnanimousBased(voters);
    }

    @Bean
    GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
        final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();
        source.set(TestServiceGrpc.getSecureMethod(),
                hasRole("ROLE_CLIENT1").and(fromClientAddress(inProcess().or(inet()))));
        source.set(TestServiceGrpc.getSecureDrainMethod(),
                hasRole("ROLE_CLIENT1").and(fromClientAddress(inProcess("test").or(inet()))));
        source.set(TestServiceGrpc.getSecureSupplyMethod(),
                hasRole("ROLE_CLIENT1").or(toServerAddress(inProcess("test-secondary"))));
        source.set(TestServiceGrpc.getSecureBidiMethod(), hasRole("ROLE_CLIENT1"));
        source.setDefault(permitAll());
        return source;
    }

}

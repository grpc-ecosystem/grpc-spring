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

package net.devh.boot.grpc.examples.security.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.CallCredentials;
import net.devh.boot.grpc.client.inject.StubTransformer;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;

/**
 * The security configuration for the client. In this case we assume that we use the same passwords for all stubs. If
 * you need per stub credentials you can delete the grpcCredentials and define a {@link StubTransformer} yourself.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @see CallCredentialsHelper
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    @Value("${auth.username}")
    private String username;

    @Bean
    // Create credentials for username + password.
    CallCredentials grpcCredentials() {
        return CallCredentialsHelper.basicAuth(this.username, this.username + "Password");
    }

}

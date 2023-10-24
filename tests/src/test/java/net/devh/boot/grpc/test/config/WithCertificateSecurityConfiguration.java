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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.SSLContextGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.X509CertificateAuthenticationProvider;

@Slf4j
@Configuration
public class WithCertificateSecurityConfiguration {

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            log.debug("Searching user: {}", username);
            final List<SimpleGrantedAuthority> authorities =
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_" + username.toUpperCase()));
            return new User(username, "", authorities);
        };
    }

    @Bean
    AuthenticationManager authenticationManager() {
        final List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(new X509CertificateAuthenticationProvider(userDetailsService()));
        return new ProviderManager(providers);
    }

    @Bean
    GrpcAuthenticationReader authenticationReader() {
        final List<GrpcAuthenticationReader> readers = new ArrayList<>();
        readers.add(new SSLContextGrpcAuthenticationReader());
        return new CompositeGrpcAuthenticationReader(readers);
    }

}

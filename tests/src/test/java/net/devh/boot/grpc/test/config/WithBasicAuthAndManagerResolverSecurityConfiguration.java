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

import com.google.common.collect.ImmutableMap;
import io.grpc.CallCredentials;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.StubTransformer;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import net.devh.boot.grpc.server.security.authentication.*;
import net.devh.boot.grpc.server.security.interceptors.GrpcServerRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.devh.boot.grpc.client.security.CallCredentialsHelper.basicAuth;
import static net.devh.boot.grpc.common.security.SecurityConstants.AUTHORIZATION_HEADER;

@Slf4j
@Configuration
public class WithBasicAuthAndManagerResolverSecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            log.debug("Searching user: {}", username);
            if (username.length() > 10) {
                throw new UsernameNotFoundException("Could not find user!");
            }
            final List<SimpleGrantedAuthority> authorities =
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_" + username.toUpperCase()));
            return new User(username, passwordEncoder().encode(username), authorities);
        };
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        final DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    AuthenticationManagerResolver<GrpcServerRequest> authenticationManager() {
        return context -> {
            String token = context.headers().get(AUTHORIZATION_HEADER);
            if (token != null && token.startsWith("Basic")) {
                final List<AuthenticationProvider> providers = new ArrayList<>();
                providers.add(daoAuthenticationProvider());
                return new ProviderManager(providers);
            } else {
                return null;
            }
        };
    }

    @Bean
    GrpcAuthenticationReader authenticationReader() {
        final List<GrpcAuthenticationReader> readers = new ArrayList<>();
        readers.add(new BasicGrpcAuthenticationReader());
        return new CompositeGrpcAuthenticationReader(readers);
    }

    // Client-Side

    @Bean
    StubTransformer mappedCredentialsStubTransformer() {
        return CallCredentialsHelper.mappedCredentialsStubTransformer(ImmutableMap.<String, CallCredentials>builder()
                .put("test", testCallCredentials("client1"))
                .put("test-secondary", testCallCredentials("client1"))
                .put("noPerm", testCallCredentials("client2"))
                .put("noPerm-secondary", testCallCredentials("client2"))
                .put("unknownUser", testCallCredentials("unknownUser"))
                // .put("noAuth", null)
                .build());
    }

    private CallCredentials testCallCredentials(final String username) {
        return basicAuth(username, username);
    }

}

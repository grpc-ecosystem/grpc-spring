/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.test.grpc.config;

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
import net.devh.springboot.autoconfigure.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.springboot.autoconfigure.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.springboot.autoconfigure.grpc.server.security.authentication.SSLContextGrpcAuthenticationReader;
import net.devh.springboot.autoconfigure.grpc.server.security.authentication.X509CertificateAuthenticationProvider;

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

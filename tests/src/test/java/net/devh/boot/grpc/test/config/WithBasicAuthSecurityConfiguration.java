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

package net.devh.boot.grpc.test.config;

import static net.devh.boot.grpc.client.security.AuthenticatingClientInterceptors.callCredentialsInterceptor;
import static net.devh.boot.grpc.client.security.CallCredentialsHelper.basicAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import net.devh.boot.grpc.client.channelfactory.InProcessChannelFactory;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;
import net.devh.boot.grpc.server.service.GrpcServiceDiscoverer;

@Slf4j
@Configuration
public class WithBasicAuthSecurityConfiguration {

    // Server-Side

    // private static final String ANONYMOUS_KEY = UUID.randomUUID().toString();

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

    // @Bean
    // AnonymousAuthenticationProvider anonymousAuthenticationProvider() {
    // return new AnonymousAuthenticationProvider(ANONYMOUS_KEY);
    // }

    @Bean
    AuthenticationManager authenticationManager() {
        final List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(daoAuthenticationProvider());
        // providers.add(anonymousAuthenticationProvider());
        return new ProviderManager(providers);
    }

    @Bean
    GrpcAuthenticationReader authenticationReader() {
        final List<GrpcAuthenticationReader> readers = new ArrayList<>();
        readers.add(new BasicGrpcAuthenticationReader());
        // readers.add(new AnonymousAuthenticationReader(ANONYMOUS_KEY));
        return new CompositeGrpcAuthenticationReader(readers);
    }

    @Bean // For testing only
    GrpcServerFactory testServerFactory(final GrpcServerProperties properties,
            final GrpcServiceDiscoverer serviceDiscoverer) {
        final InProcessGrpcServerFactory factory = new InProcessGrpcServerFactory("test", properties);
        for (final GrpcServiceDefinition service : serviceDiscoverer.findGrpcServices()) {
            factory.addService(service);
        }
        return factory;
    }

    // Client-Side

    @Bean
    @SuppressWarnings("deprecation") // Used to fake authentication based on the client's name (including Channels)
    ClientInterceptor basicAuthInterceptor() {
        // return AuthenticatingClientInterceptors.basicAuthInterceptor("client1", "client1");
        return callCredentialsInterceptor(basicAuth("client1", "client1"));
    }

    @Bean // For testing only
    @SuppressWarnings("deprecation") // Used to fake authentication based on the client's name (including Channels)
    GrpcChannelFactory testChannelFactory(final GrpcChannelsProperties properties,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
            final List<GrpcChannelConfigurer> channelConfigurers) {
        return new InProcessChannelFactory(properties, globalClientInterceptorRegistry, channelConfigurers) {

            @Override
            public Channel createChannel(final String name, List<ClientInterceptor> interceptors) {
                if ("bean".equals(name)) {
                    // We use the GrpcClient#interceptorNames() here.
                    return super.createChannel("test", interceptors);
                }
                interceptors = new ArrayList<>(interceptors);
                // Fake per client authentication
                String username;
                if ("test".equals(name)) {
                    username = "client1";
                } else if ("noPerm".equals(name)) {
                    username = "client2";
                } else if ("unknownUser".equals(name)) {
                    username = "unknownUser";
                } else if ("noAuth".equals(name)) {
                    return super.createChannel("test", interceptors);
                } else {
                    throw new IllegalArgumentException("Unknown username: " + name);
                }
                interceptors.add(callCredentialsInterceptor(basicAuth(username, username)));
                return super.createChannel("test", interceptors);
            }

        };
    }

}

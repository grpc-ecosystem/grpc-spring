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

package net.devh.boot.grpc.common.security;

import java.nio.charset.StandardCharsets;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;

/**
 * A helper class with constants related to grpc security.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class SecurityConstants {

    /**
     * A convenience constant that contains the key for the HTTP Authorization Header.
     */
    public static final Key<String> AUTHORIZATION_HEADER = Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * The prefix for basic auth as used in the {@link #AUTHORIZATION_HEADER}. This library assumes that the both the
     * username and password are {@link StandardCharsets#UTF_8 UTF_8} encoded before being turned into a base64 string.
     */
    public static final String BASIC_AUTH_PREFIX = "Basic ";

    /**
     * The prefix for bearer auth as used in the {@link #AUTHORIZATION_HEADER}.
     */
    public static final String BEARER_AUTH_PREFIX = "Bearer ";

    private SecurityConstants() {}

}

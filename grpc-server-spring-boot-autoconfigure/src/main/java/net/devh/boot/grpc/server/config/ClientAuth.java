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

package net.devh.boot.grpc.server.config;

import javax.net.ssl.SSLEngine;

/**
 * Indicates the state of the {@link SSLEngine} with respect to client authentication. This configuration item really
 * only applies when building the server-side SslContext.
 */
public enum ClientAuth {

    /**
     * Indicates that the {@link SSLEngine} will not request client authentication.
     */
    NONE,

    /**
     * Indicates that the {@link SSLEngine} will request client authentication.
     */
    OPTIONAL,

    /**
     * Indicates that the {@link SSLEngine} will <b>require</b> client authentication.
     */
    REQUIRE;

}

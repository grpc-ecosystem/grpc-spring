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

package net.devh.boot.grpc.client.config;

/**
 * Identifies the negotiation used for starting up HTTP/2.
 *
 * @see io.grpc.netty.shaded.io.grpc.netty.NegotiationType NegotiationType
 */
// This class needs to be duplicated to avoid direct dependencies to either of the grpc-netty (shaded) libraries.
public enum NegotiationType {

    /**
     * Uses TLS ALPN/NPN negotiation, assumes an SSL connection.
     */
    TLS,

    /**
     * Use the HTTP UPGRADE protocol for a plaintext (non-SSL) upgrade from HTTP/1.1 to HTTP/2.
     */
    PLAINTEXT_UPGRADE,

    /**
     * Just assume the connection is plaintext (non-SSL) and the remote endpoint supports HTTP/2 directly without an
     * upgrade.
     */
    PLAINTEXT;

}

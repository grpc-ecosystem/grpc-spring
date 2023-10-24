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

package net.devh.boot.grpc.server.security.interceptors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * Marker-Interface: A server interceptor that used to authenticate the client request.
 *
 * <p>
 * <b>Note:</b> Implementations must be thread safe and return a thread safe {@link Listener}. Do <b>NOT</b> store the
 * authentication in a thread local context (permanently). The authentication context must be cleared before returning
 * from {@link #interceptCall(ServerCall, Metadata, ServerCallHandler) interceptCall()} and all the {@link Listener}
 * methods.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @see AbstractAuthenticatingServerCallListener
 * @see Contexts#interceptCall(Context, ServerCall, Metadata, ServerCallHandler)
 */
public interface AuthenticatingServerInterceptor extends ServerInterceptor {

    /**
     * The context key that can be used to retrieve the associated {@link Authentication}.
     */
    Context.Key<SecurityContext> SECURITY_CONTEXT_KEY = Context.key("security-context");

    /**
     * The context key that can be used to retrieve the originally associated {@link Authentication}.
     *
     * @deprecated Use {@link #SECURITY_CONTEXT_KEY} instead.
     */
    @Deprecated
    Context.Key<Authentication> AUTHENTICATION_CONTEXT_KEY = Context.key("authentication");

}

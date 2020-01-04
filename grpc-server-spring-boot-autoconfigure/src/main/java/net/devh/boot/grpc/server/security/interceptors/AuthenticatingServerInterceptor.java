/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.security.interceptors;

import org.springframework.security.core.Authentication;

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
    public static final Context.Key<Authentication> AUTHENTICATION_CONTEXT_KEY = Context.key("authentication");

}

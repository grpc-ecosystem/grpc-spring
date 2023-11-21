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

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * A call listener that will set the authentication context before each invocation and clear it afterwards. Use and
 * extend this class if you want to setup non-grpc authentication contexts.
 *
 * <p>
 * <b>Note:</b> If you only want to setup the grpc-context and nothing else, then you can use
 * {@link Contexts#interceptCall(Context, ServerCall, Metadata, ServerCallHandler)} instead.
 *
 * @param <ReqT> The type of the request.
 */
@Slf4j
public abstract class AbstractAuthenticatingServerCallListener<ReqT> extends SimpleForwardingServerCallListener<ReqT> {

    private final Context context;

    /**
     * Creates a new AbstractAuthenticatingServerCallListener which will attach the given security context before
     * delegating to the given listener.
     *
     * @param delegate The listener to delegate to.
     * @param context The context to attach.
     */
    protected AbstractAuthenticatingServerCallListener(final Listener<ReqT> delegate, final Context context) {
        super(delegate);
        this.context = context;
    }

    /**
     * Gets the {@link Context} associated with the call.
     *
     * @return The context of the current call.
     */
    protected final Context context() {
        return this.context;
    }

    /**
     * Attaches the authentication context before the actual call.
     *
     * <p>
     * This method is called after the grpc context is attached.
     * </p>
     */
    protected abstract void attachAuthenticationContext();

    /**
     * Detaches the authentication context after the actual call.
     *
     * <p>
     * This method is called before the grpc context is detached.
     * </p>
     */
    protected abstract void detachAuthenticationContext();

    @Override
    public void onMessage(final ReqT message) {
        final Context previous = this.context.attach();
        try {
            attachAuthenticationContext();
            log.debug("onMessage - Authentication set");
            super.onMessage(message);
        } finally {
            detachAuthenticationContext();
            this.context.detach(previous);
            log.debug("onMessage - Authentication cleared");
        }
    }

    @Override
    public void onHalfClose() {
        final Context previous = this.context.attach();
        try {
            attachAuthenticationContext();
            log.debug("onHalfClose - Authentication set");
            super.onHalfClose();
        } finally {
            detachAuthenticationContext();
            this.context.detach(previous);
            log.debug("onHalfClose - Authentication cleared");
        }
    }

    @Override
    public void onCancel() {
        final Context previous = this.context.attach();
        try {
            attachAuthenticationContext();
            log.debug("onCancel - Authentication set");
            super.onCancel();
        } finally {
            detachAuthenticationContext();
            log.debug("onCancel - Authentication cleared");
            this.context.detach(previous);
        }
    }

    @Override
    public void onComplete() {
        final Context previous = this.context.attach();
        try {
            attachAuthenticationContext();
            log.debug("onComplete - Authentication set");
            super.onComplete();
        } finally {
            detachAuthenticationContext();
            log.debug("onComplete - Authentication cleared");
            this.context.detach(previous);
        }
    }

    @Override
    public void onReady() {
        final Context previous = this.context.attach();
        try {
            attachAuthenticationContext();
            log.debug("onReady - Authentication set");
            super.onReady();
        } finally {
            detachAuthenticationContext();
            log.debug("onReady - Authentication cleared");
            this.context.detach(previous);
        }
    }

}

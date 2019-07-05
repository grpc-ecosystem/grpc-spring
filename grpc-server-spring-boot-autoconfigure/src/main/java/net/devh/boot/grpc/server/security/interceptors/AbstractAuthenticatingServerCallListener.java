/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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

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

package net.devh.boot.grpc.web.bridge;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import io.grpc.BindableService;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.web.bridge.GrpcMethodWrapper.WrappedServerCall;
import net.devh.boot.grpc.web.conversion.PojoFormat;

/**
 * The grpc controller that bridges the web requests to the grpc-service implementations.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@RestController
@RequestMapping("/grpc")
public class GrpcWebController {

    private static final Logger log = LoggerFactory.getLogger(GrpcWebController.class);

    /**
     * A map with the fully qualified method names and the associated grpc methods.
     */
    protected final Map<String, GrpcMethodWrapper<?, ?>> methods = new LinkedHashMap<>();

    protected final PojoFormat format;

    /**
     * Creates a new grpc rest controller with the given map format.
     *
     * @param format The map format used to translate the incoming generic maps to protobuf types.
     */
    public GrpcWebController(final PojoFormat format) {
        this.format = requireNonNull(format, "format");
    }

    /**
     * Registers the given service with all of its method in this controller and thus make them accessible via web.
     *
     * @param service The service to register.
     */
    public void register(final BindableService service) {
        register(service.bindService());
    }

    /**
     * Registers the given service definition with all of its method in this controller and thus make them accessible
     * via web.
     *
     * @param service The service to register.
     */
    public void register(final ServerServiceDefinition service) {
        for (final ServerMethodDefinition<?, ?> method : service.getMethods()) {
            register(method);
        }
    }

    /**
     * Registers the given method in this controller and thus make it accessible via web.
     *
     * @param method The method to register.
     */
    public void register(final ServerMethodDefinition<?, ?> method) {
        final GrpcMethodWrapper<?, ?> wrapper = configure(GrpcMethodWrapper.ofRaw(method));
        this.methods.put(wrapper.getFullMethodName(), wrapper);
        this.format.register(wrapper.getRequestDescriptor(), wrapper.getRequestBuilderSupplier());
        log.debug("Registered web-grpc-bridge for {}", wrapper.getFullMethodName());
    }

    /**
     * Hook that can be used to configure the given grpc method wrapper. Subclasses can overwrite this method to
     * configure interceptors. This method is called during the {@link #register(ServerMethodDefinition) grpc method
     * registration}.The default implementation does nothing.
     *
     * @param wrapper The wrapper to configure.
     * @return The configured wrapper.
     */
    protected GrpcMethodWrapper<?, ?> configure(final GrpcMethodWrapper<?, ?> wrapper) {
        // Empty - @Overrideable in subclasses
        return wrapper;
    }

    /**
     * Gets a set that contains all fully qualified method names that are supported by this controller.
     *
     * @return A set with all registered method names.
     */
    @GetMapping
    public Set<String> allMethods() {
        return this.methods.keySet();
    }

    /**
     * Calls the specified grpc service method.
     *
     * <p>
     * <b>Note:</b> This call blocks until the grpc method completes or errors.
     * </p>
     *
     * @param service The fully qualified service name to call.
     * @param method The method name inside the service to call.
     * @param request The request body. Can either be a single request or a collection of many request.
     * @return Either a single response or a list of responses.
     * @throws InvalidProtocolBufferException If something went wrong during the transformation of the protobuf classes.
     * @throws StatusException If the method returned with a non-OK status code.
     * @throws StatusRuntimeException If something went wrong during the grpc stub calls.
     */
    @PostMapping(path = "/{service}/{method}")
    public Object call(
            @PathVariable("service") final String service,
            @PathVariable("method") final String method,
            @RequestBody final Object request)
            throws InvalidProtocolBufferException, StatusException {
        return call(MethodDescriptor.generateFullMethodName(service, method), request);
    }

    /**
     * Calls the specified grpc service method.
     *
     * <p>
     * <b>Note:</b> This call blocks until the grpc method completes or errors.
     * </p>
     *
     * @param function The fully qualified method name.
     * @param request The request body. Can either be a single request or a collection of many request.
     * @return Either a single response or a list of responses.
     * @throws InvalidProtocolBufferException If something went wrong during the transformation of the protobuf classes.
     * @throws StatusException If the method returned with a non-OK status code.
     * @throws StatusRuntimeException If something went wrong during the grpc stub calls.
     */
    public Object call(final String function, final Object request)
            throws InvalidProtocolBufferException, StatusException {
        final GrpcMethodWrapper<?, ?> grpcMethod = this.methods.get(function);
        return transformAndInvoke(grpcMethod, request, new Metadata());
    }

    /**
     * Transforms the given request(s) to protobuf, executes the specified grpc service method and then transform the
     * results back to the original.
     *
     * <p>
     * <b>Note:</b> This call blocks until the grpc method completes or errors.
     * </p>
     *
     * @param <RequestT> The type of the request(s).
     * @param <ResponseT> The type of the response(s).
     * @param grpcMethod The grpc method to call.
     * @param request Either a single response or a list of responses.
     * @param headers The headers that were send along with the request.
     * @return Either a single response or a list of responses.
     * @throws InvalidProtocolBufferException If something went wrong during the transformation of the protobuf classes.
     * @throws StatusException If the method returned with a non-OK status code.
     * @throws StatusRuntimeException If something went wrong during the grpc stub calls.
     */
    public <RequestT extends Message, ResponseT extends Message> Object transformAndInvoke(
            final GrpcMethodWrapper<RequestT, ResponseT> grpcMethod,
            final Object request, final Metadata headers)
            throws InvalidProtocolBufferException, StatusException {
        final Descriptor requestDescriptor = grpcMethod.getRequestDescriptor();

        final GrpcMethodResult<ResponseT> result =
                invoke(grpcMethod, headers, this.format.<RequestT>toManyMessages(requestDescriptor, request));

        if (result.wasSuccessful()) {
            return this.format.convert(result.getMessages());
        } else {
            throw new StatusException(result.getStatus(), result.getHeaders());
        }
    }

    /**
     * Invokes the given grpc method and passes the given requests to it.
     *
     * <p>
     * <b>Note:</b> This call blocks until the grpc method completes or errors.
     * </p>
     *
     * @param <RequestT> The type of the request(s).
     * @param <ResponseT> The type of the response(s).
     * @param grpcMethod The grpc method to invoke.
     * @param headers The headers that should be send along with the request.
     * @param requests The requests that should be send to the method.
     * @return The grpc method result.
     */
    public <RequestT extends Message, ResponseT extends Message> GrpcMethodResult<ResponseT> invoke(
            final GrpcMethodWrapper<RequestT, ResponseT> grpcMethod,
            final Metadata headers,
            final Iterable<RequestT> requests) {

        final WrappedServerCall<RequestT, ResponseT> serverCall = grpcMethod.prepare();
        final Listener<RequestT> listener = grpcMethod.startCall(serverCall, headers);

        try {
            listener.onReady();
            for (final RequestT message : requests) {
                listener.onMessage(message);
            }
            listener.onHalfClose();
            listener.onComplete();
            return serverCall.getResult();
        } catch (final StatusRuntimeException e) {
            listener.onCancel();
            return GrpcMethodResult.from(e);
        } catch (final RuntimeException e) {
            listener.onCancel();
            return new GrpcMethodResult<>(Status.INTERNAL
                    .withDescription("Error executing " + grpcMethod.getFullMethodName() + ": " + e.getMessage())
                    .withCause(e));
        }
    }

}

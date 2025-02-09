/*
 * Copyright (c) 2016-2025 The gRPC-Spring Authors
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

package net.devh.boot.grpc.server.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.alibaba.nacos.common.utils.MapUtil;
import com.netflix.discovery.shared.Pair;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * {@code RequestValidatingInterceptor} is a gRPC server interceptor that validates incoming requests using the
 * {@link BaseValidator} interface and performs custom validations as defined by {@link GrpcValidator}.
 * <p>
 * This interceptor is registered globally using the {@link GrpcGlobalServerInterceptor} annotation and is invoked
 * for every gRPC call on the server. It first validates the request using the base validation logic provided
 * by the {@link BaseValidator} interface, and then it performs additional custom validation based on the
 * {@link GrpcValidator} annotations discovered by the {@link GrpcValidatorDiscoverer}.
 * </p>
 *
 * @author Pritesh (priteshdpawar53@gmail.com)
 * @since 02/10/25
 */
@GrpcGlobalServerInterceptor
@Slf4j
public class RequestValidatingInterceptor implements ServerInterceptor, BaseValidator {

    private final GrpcValidatorDiscoverer validatorDiscoverer;

    /**
     * Constructs a new {@code RequestValidatingInterceptor} with the given {@link GrpcValidatorDiscoverer}.
     * The {@link GrpcValidatorDiscoverer} is used to discover and retrieve custom validators for request classes.
     *
     * @param validatorDiscoverer the {@link GrpcValidatorDiscoverer} used for discovering validators
     */
    public RequestValidatingInterceptor(GrpcValidatorDiscoverer validatorDiscoverer) {
        this.validatorDiscoverer = validatorDiscoverer;
    }

    /**
     * Intercepts an incoming gRPC request and performs validation on the message.
     * <p>
     * This method is called when a request is received. It first validates the request using the base validation
     * provided by {@link BaseValidator}, and then it performs custom validation as defined by the
     * {@link GrpcValidator} annotations on methods in service beans.
     * </p>
     *
     * @param <ReqT>               the type of the request message
     * @param <RespT>              the type of the response message
     * @param serverCall           the gRPC server call
     * @param metadata             the metadata associated with the call
     * @param serverCallHandler    the handler to which the call is delegated
     * @return a listener for the request
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        final ServerCall.Listener<ReqT> delegate = serverCallHandler.startCall(serverCall, metadata);

        // Wrapping the listener to perform custom validation on incoming message
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onMessage(ReqT message) {
                log.info("Validating incoming protobuf message: {}", message);
                validate(message);  // Perform base validation
                handleCustomValidations(message);  // Perform custom validation
                super.onMessage(message);  // Proceed with the request
            }
        };
    }

    /**
     * Handles custom validations for the incoming request message based on the {@link GrpcValidator} annotations
     * and the discovered validator methods in the {@link GrpcValidatorDiscoverer}.
     * <p>
     * If a custom validation method is found for the given message's class, it invokes the validator method on the
     * corresponding validator bean.
     * </p>
     *
     * @param message the request message to validate
     * @param <ReqT>  the type of the request message
     * @throws StatusRuntimeException if validation fails, throws an exception with appropriate status and details
     */
    private <ReqT> void handleCustomValidations(ReqT message) {
        // Retrieve the map of request validators from the discoverer
        Map<Class<?>, Pair<Object, String>> requestValidatorMethodMap =
                validatorDiscoverer.getRequestValidatorMethodMap();

        // If there are no validators for the message class, skip custom validation
        if (MapUtil.isEmpty(requestValidatorMethodMap) || !requestValidatorMethodMap.containsKey(message.getClass())) {
            return;
        }

        // Retrieve the validator bean and method name for the message class
        Pair<Object, String> validatorBeanPair = requestValidatorMethodMap.get(message.getClass());
        Method validatorMethod;
        try {
            // Retrieve the validator method
            validatorMethod = validatorBeanPair.first().getClass().getDeclaredMethod(validatorBeanPair.second(), message.getClass());
            // Invoke the validator method
            validatorMethod.invoke(validatorBeanPair.first(), message);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // Handle errors during method invocation
            throw new StatusRuntimeException(Status.INTERNAL.withCause(e).withDescription(e.getMessage()));
        } catch (InvocationTargetException e) {
            // Handle errors thrown by the validator method itself
            throw new StatusRuntimeException(Status.fromThrowable(e).withDescription(e.getMessage()));
        }
    }
}

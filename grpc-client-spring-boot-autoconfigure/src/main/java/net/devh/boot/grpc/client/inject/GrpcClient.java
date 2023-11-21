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

package net.devh.boot.grpc.client.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.stub.AbstractStub;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelProperties.Security;

/**
 * An annotation for fields of type {@link Channel} or subclasses of {@link AbstractStub}/gRPC client services. Also
 * works for annotated methods that only take a single parameter of these types. Annotated fields/methods will be
 * automatically populated/invoked by Spring.
 *
 * <p>
 * <b>Note:</b> Fields/Set-Methods that are annotated with this annotation should NOT be annotated with
 * {@link Autowired} or {@link Inject} (conflict).
 * </p>
 *
 * <p>
 * <b>Note:</b> If you annotate an AbstractStub with this annotation the bean processing will also apply the
 * {@link StubTransformer}s in the application context. These can be used, for example, to configure {@link CallOptions}
 * such as {@link CallCredentials}. Please note that these transformations aren't applied if you inject a
 * {@link Channel} only.
 * </p>
 *
 * <p>
 * <b>Note:</b> These annotation allows the specification of custom interceptors. These will be appended to the global
 * interceptors and applied using {@link ClientInterceptors#interceptForward(Channel, ClientInterceptor...)}.
 * </p>
 * 
 * <p>
 * <b>Note:</b> The constructor and bean factory injection is experimental.
 * </p>
 *
 * @see GrpcClientBean Add as bean to the {@link ApplicationContext}.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GrpcClient {

    /**
     * The name of the grpc client. This name will be used to get the {@link GrpcChannelProperties config options} for
     * this client.
     *
     * <p>
     * <b>Example:</b> <code>@GrpcClient("myClient")</code> &lt;-&gt;
     * {@code grpc.client.myClient.address=static://localhost:9090}
     * </p>
     *
     * <p>
     * <b>Note:</b> This value might also be used to check the common / alternative names in server certificate, you can
     * overwrite this value with the {@link Security security.authorityOverride} property.
     * </p>
     *
     * @return The name of the grpc client.
     */
    String value();

    /**
     * A list of {@link ClientInterceptor} classes that should be used with this client in addition to the globally
     * defined ones. If a bean of the given type exists, it will be used; otherwise a new instance of that class will be
     * created via no-args constructor.
     *
     * <p>
     * <b>Note:</b> Please read the javadocs regarding the ordering of interceptors.
     * </p>
     *
     * @return A list of ClientInterceptor classes that should be used.
     */
    Class<? extends ClientInterceptor>[] interceptors() default {};

    /**
     * A list of {@link ClientInterceptor} beans that should be used with this client in addition to the globally
     * defined ones.
     *
     * <p>
     * <b>Note:</b> Please read the javadocs regarding the ordering of interceptors.
     * </p>
     *
     * @return A list of ClientInterceptor beans that should be used.
     */
    String[] interceptorNames() default {};

    /**
     * Whether the custom interceptors should be mixed with the global interceptors and sorted afterwards. Use this
     * option if you want to add a custom interceptor between global interceptors.
     *
     * @return True, if the custom interceptors should be merged with the global ones and sorted afterwards. False
     *         otherwise.
     */
    boolean sortInterceptors() default false;

}

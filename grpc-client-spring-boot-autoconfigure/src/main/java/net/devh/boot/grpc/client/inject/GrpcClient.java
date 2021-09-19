/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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
 * <b>Note:</b> Fields/Methods that are annotated with this annotation should NOT be annotated with {@link Autowired} or
 * {@link Inject} (conflict).
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
 * @see GrpcClientBean Add as bean to the {@link ApplicationContext}.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
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

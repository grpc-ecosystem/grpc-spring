/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.server.security.check;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import net.devh.boot.grpc.server.security.authentication.AnonymousAuthenticationReader;

/**
 * A {@link GrpcSecurityMetadataSource} for manual configuration. For each {@link MethodDescriptor gRPC method} a
 * {@link AccessPredicate} can be defined, that checks whether the user is authenticated and has access. If you want to
 * allow public access then use {@code null} as the AccessPredicate parameter or enable
 * {@link AnonymousAuthenticationReader anonymous authentication}. This metadata source only works if an
 * {@link AccessDecisionManager} is configured with an {@link AccessPredicateVoter}.
 *
 * <p>
 * <b>Note:</b> This instance is initialized with {@link AccessPredicate#denyAll() deny all} as default.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class ManualGrpcSecurityMetadataSource extends AbstractGrpcSecurityMetadataSource {

    private final Map<MethodDescriptor<?, ?>, Collection<ConfigAttribute>> accessMap = new HashMap<>();
    private Collection<ConfigAttribute> defaultAttributes = wrap(AccessPredicate.denyAll());

    @Override
    public Collection<ConfigAttribute> getAttributes(final MethodDescriptor<?, ?> method) {
        return this.accessMap.getOrDefault(method, this.defaultAttributes);
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return this.accessMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Set the given access predicate for the all methods of the given service. This will replace previously set
     * predicates.
     *
     * @param service The service to protect with a custom check.
     * @param predicate The predicate used to check the {@link Authentication}. IIf set to null it will use the public
     *        access.
     * @see #setDefault(AccessPredicate)
     */
    public void set(final ServiceDescriptor service, @Nullable final AccessPredicate predicate) {
        requireNonNull(service, "service");
        final Collection<ConfigAttribute> wrappedPredicate = wrap(predicate);
        for (final MethodDescriptor<?, ?> method : service.getMethods()) {
            this.accessMap.put(method, wrappedPredicate);
        }
    }

    /**
     * Removes all access predicates for the all methods of the given service. After that, the default will be used for
     * those methods.
     *
     * @param service The service to protect with only the default.
     * @see #setDefault(AccessPredicate)
     */
    public void remove(final ServiceDescriptor service) {
        requireNonNull(service, "service");
        for (final MethodDescriptor<?, ?> method : service.getMethods()) {
            this.accessMap.remove(method);
        }
    }

    /**
     * Set the given access predicate for the given method. This will replace previously set predicates.
     *
     * @param method The method to protect with a custom check.
     * @param predicate The predicate used to check the {@link Authentication}. If set to null it will use the public
     *        access.
     * @see #setDefault(AccessPredicate)
     */
    public void set(final MethodDescriptor<?, ?> method, @Nullable final AccessPredicate predicate) {
        requireNonNull(method, "method");
        this.accessMap.put(method, wrap(predicate));
    }

    /**
     * Removes the any access predicate for the given method. After that, the default will be used for that method.
     *
     * @param method The method to protect with only the default.
     * @see #setDefault(AccessPredicate)
     */
    public void remove(final MethodDescriptor<?, ?> method) {
        requireNonNull(method, "method");
        this.accessMap.remove(method);
    }

    /**
     * Sets the default that will be used if no specific configuration has been made.
     *
     * @param predicate The default predicate used to check the {@link Authentication}. If set to null it will use the
     *        public access.
     */
    public void setDefault(final AccessPredicate predicate) {
        this.defaultAttributes = wrap(predicate);
    }

    /**
     * Wraps the given predicate in a configuration attribute and an immutable collection.
     *
     * @param predicate The predicate to wrap.
     * @return The newly created list with the given predicate.
     */
    protected Collection<ConfigAttribute> wrap(final AccessPredicate predicate) {
        if (predicate == null) {
            return of(); // Empty collection => public invocation
        }
        return of(new AccessPredicateConfigAttribute(predicate));
    }

}

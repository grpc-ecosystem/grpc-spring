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

package net.devh.boot.grpc.server.security.check;

import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.ImmutableSet;

import io.grpc.Grpc;
import io.grpc.ServerCall;
import io.grpc.inprocess.InProcessSocketAddress;

/**
 * Predicate that can be used to check whether the given {@link Authentication} has access to the protected
 * service/method. This interface assumes, that the user is authenticated before the method is called.
 *
 * @author Daniel Theuke (daniel.theuke@aequitas-software.de)
 */
public interface AccessPredicate extends BiPredicate<Authentication, ServerCall<?, ?>> {

    /**
     * Checks whether the given user is authorized to execute the given call.
     *
     * @param authentication The authentication to check.
     * @param serverCall The secure object being called.
     * @return True, if the user has access. False otherwise.
     */
    @Override
    boolean test(Authentication authentication, ServerCall<?, ?> serverCall);

    @Override
    default AccessPredicate negate() {
        return (a, c) -> !test(a, c);
    }

    /**
     * Combines this predicate with the given predicate using the {@code AND} operator.
     *
     * @param other The other predicate to call.
     * @return The combined predicate.
     */
    default AccessPredicate and(final Predicate<? super Authentication> other) {
        requireNonNull(other);
        return (a, c) -> test(a, c) && other.test(a);
    }

    @Override
    default AccessPredicate and(final BiPredicate<? super Authentication, ? super ServerCall<?, ?>> other) {
        requireNonNull(other);
        return (a, c) -> test(a, c) && other.test(a, c);
    }

    /**
     * Combines this predicate with the given predicate using the {@code OR} operator.
     *
     * @param other The other predicate to call.
     * @return The combined predicate.
     */
    default AccessPredicate or(final Predicate<? super Authentication> other) {
        requireNonNull(other);
        return (a, c) -> test(a, c) || other.test(a);
    }

    @Override
    default AccessPredicate or(final BiPredicate<? super Authentication, ? super ServerCall<?, ?>> other) {
        requireNonNull(other);
        return (a, c) -> test(a, c) || other.test(a, c);
    }

    /**
     * Special constant that symbolizes that everybody (including unauthenticated users) can access the instance (no
     * protection).
     *
     * <p>
     * <b>Note:</b> This is a special constant, that does not allow execution and mutation. It's sole purpose is to
     * avoid ambiguity for {@code null} values. It should only be used in {@code ==} comparisons.
     * </p>
     *
     * @return A special constant that symbolizes public access.
     */
    static AccessPredicate permitAll() {
        return AccessPredicates.PERMIT_ALL;
    }

    /**
     * All authenticated users can access the protected instance including anonymous users.
     *
     * <p>
     * <b>Note:</b> The negation of this call is {@link #denyAll()} and NOT all unauthenticated.
     * </p>
     *
     * @return A newly created AccessPredicate that always returns true.
     */
    static AccessPredicate authenticated() {
        return (a, c) -> true;
    }

    /**
     * All authenticated users can access the protected instance excluding anonymous users.
     *
     * @return A newly created AccessPredicate that checks whether the user is explicitly authenticated.
     */
    static AccessPredicate fullyAuthenticated() {
        return (a, c) -> !(a instanceof AnonymousAuthenticationToken);
    }

    /**
     * Nobody can access the protected instance.
     *
     * <p>
     * <b>Note:</b> The negation of this call is {@link #authenticated()} and NOT {@link #permitAll()}.
     * </p>
     *
     * @return A newly created AccessPredicate that always returns false.
     */
    static AccessPredicate denyAll() {
        return (a, c) -> false;
    }

    /**
     * Only those who have the given role can access the protected instance.
     *
     * @param role The role to check for.
     * @return A newly created AccessPredicate that only returns true, if the name of the {@link GrantedAuthority}s
     *         matches the given role name.
     */
    static AccessPredicate hasRole(final String role) {
        requireNonNull(role, "role");
        return (a, c) -> {
            for (final GrantedAuthority authority : a.getAuthorities()) {
                if (role.equals(authority.getAuthority())) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Only those who have the given {@link GrantedAuthority} can access the protected instance.
     *
     * @param role The role to check for.
     * @return A newly created AccessPredicate that only returns true, if the {@link GrantedAuthority}s matches the
     *         given role.
     */
    static AccessPredicate hasAuthority(final GrantedAuthority role) {
        requireNonNull(role, "role");
        return (a, c) -> {
            for (final GrantedAuthority authority : a.getAuthorities()) {
                if (role.equals(authority)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Only those who have any of the given roles can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the name of the {@link GrantedAuthority}s
     *         matches any of the given role names.
     */
    static AccessPredicate hasAnyRole(final String... roles) {
        requireNonNull(roles, "roles");
        return hasAnyRole(Arrays.asList(roles));
    }

    /**
     * Only those who have any of the given roles can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the name of the {@link GrantedAuthority}s
     *         matches any of the given role names.
     */
    static AccessPredicate hasAnyRole(final Collection<String> roles) {
        requireNonNull(roles, "roles");
        roles.forEach(role -> requireNonNull(role, "role"));
        final Set<String> immutableRoles = ImmutableSet.copyOf(roles);
        return (a, c) -> {
            for (final GrantedAuthority authority : a.getAuthorities()) {
                if (immutableRoles.contains(authority.getAuthority())) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Only those who have any of the given {@link GrantedAuthority} can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the {@link GrantedAuthority}s matches any of
     *         the given roles.
     */
    static AccessPredicate hasAnyAuthority(final GrantedAuthority... roles) {
        requireNonNull(roles, "roles");
        return hasAnyAuthority(Arrays.asList(roles));
    }

    /**
     * Only those who have any of the given {@link GrantedAuthority} can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the {@link GrantedAuthority}s matches any of
     *         the given roles.
     */
    static AccessPredicate hasAnyAuthority(final Collection<GrantedAuthority> roles) {
        requireNonNull(roles, "roles");
        roles.forEach(role -> requireNonNull(role, "role"));
        final Set<GrantedAuthority> immutableRoles = ImmutableSet.copyOf(roles);
        return (a, c) -> {
            for (final GrantedAuthority authority : a.getAuthorities()) {
                if (immutableRoles.contains(authority)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Only those who have all of the given roles can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the name of the {@link GrantedAuthority}s
     *         matches all of the given role names.
     */
    static AccessPredicate hasAllRoles(final String... roles) {
        requireNonNull(roles, "roles");
        return hasAnyRole(Arrays.asList(roles));
    }

    /**
     * Only those who have all of the given roles can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the name of the {@link GrantedAuthority}s
     *         matches all of the given role names.
     */
    static AccessPredicate hasAllRoles(final Collection<String> roles) {
        requireNonNull(roles, "roles");
        roles.forEach(role -> requireNonNull(role, "role"));
        final Set<String> immutableRoles = ImmutableSet.copyOf(roles);
        return (a, c) -> {
            for (final GrantedAuthority authority : a.getAuthorities()) {
                if (!immutableRoles.contains(authority.getAuthority())) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Only those who have all of the given {@link GrantedAuthority} can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the {@link GrantedAuthority}s matches all of
     *         the given roles.
     */
    static AccessPredicate hasAllAuthorities(final GrantedAuthority... roles) {
        requireNonNull(roles, "roles");
        return hasAllAuthorities(Arrays.asList(roles));
    }

    /**
     * Only those who have any of the given {@link GrantedAuthority} can access the protected instance.
     *
     * @param roles The roles to check for.
     * @return A newly created AccessPredicate that only returns true, if the {@link GrantedAuthority}s matches all of
     *         the given roles.
     */
    static AccessPredicate hasAllAuthorities(final Collection<GrantedAuthority> roles) {
        requireNonNull(roles, "roles");
        roles.forEach(role -> requireNonNull(role, "role"));
        final Set<GrantedAuthority> immutableRoles = ImmutableSet.copyOf(roles);
        return (a, c) -> {
            for (final GrantedAuthority authority : a.getAuthorities()) {
                if (!immutableRoles.contains(authority)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Checks that the client connected from the given address.
     *
     * @param remoteAddressCheck The check to apply to the client address.
     * @return A newly created AccessPredicate that only returns true, if the client address passes the given check.
     *
     * @see Grpc#TRANSPORT_ATTR_REMOTE_ADDR
     */
    static AccessPredicate fromClientAddress(final Predicate<? super SocketAddress> remoteAddressCheck) {
        requireNonNull(remoteAddressCheck, "remoteAddressCheck");
        return (a, c) -> remoteAddressCheck.test(c.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));
    }

    /**
     * Checks that the client connected to the given server address.
     *
     * @param localAddressCheck The check to apply to the server address.
     * @return A newly created AccessPredicate that only returns true, if the server address passes the given check.
     *
     * @see Grpc#TRANSPORT_ATTR_LOCAL_ADDR
     */
    static AccessPredicate toServerAddress(final Predicate<? super SocketAddress> localAddressCheck) {
        requireNonNull(localAddressCheck, "localAddressCheck");
        return (a, c) -> localAddressCheck.test(c.getAttributes().get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR));
    }

    /**
     * Some helper methods used to create {@link Predicate}s for {@link SocketAddress}es.
     */
    interface SocketPredicate extends Predicate<SocketAddress> {

        /**
         * Checks the type of the socket address.
         *
         * @param type The expected class of the socket address.
         * @return The newly created socket predicate.
         */
        static SocketPredicate type(final Class<? extends SocketAddress> type) {
            requireNonNull(type, "type");
            return type::isInstance;
        }

        /**
         * Checks the type of the socket address and the given condition.
         *
         * @param <T> The expected type of the socket address.
         * @param type The expected class of the socket address.
         * @param condition The additional condition the socket has to pass.
         * @return The newly created socket predicate.
         */
        @SuppressWarnings("unchecked")
        static <T> SocketPredicate typeAnd(final Class<T> type, final Predicate<T> condition) {
            requireNonNull(type, "type");
            requireNonNull(condition, "condition");
            return s -> type.isInstance(s) && condition.test((T) s);
        }

        /**
         * Checks that the given socket address is an {@link InProcessSocketAddress}.
         *
         * @return The newly created socket predicate.
         */
        static SocketPredicate inProcess() {
            return type(InProcessSocketAddress.class);
        }

        /**
         * Checks that the given socket address is an {@link InProcessSocketAddress} with the given name.
         *
         * @param name The name of in process connection.
         * @return The newly created socket predicate.
         */
        static SocketPredicate inProcess(final String name) {
            requireNonNull(name, "name");
            return typeAnd(InProcessSocketAddress.class, s -> name.equals(s.getName()));
        }

        /**
         * Checks that the given socket address is a {@link InetSocketAddress}.
         *
         * @return The newly created socket predicate.
         */
        static SocketPredicate inet() {
            return type(InetSocketAddress.class);
        }

    }

}

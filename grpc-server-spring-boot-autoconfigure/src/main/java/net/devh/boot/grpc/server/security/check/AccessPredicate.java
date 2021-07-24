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

package net.devh.boot.grpc.server.security.check;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.ImmutableSet;

/**
 * Predicate that can be used to check whether the given {@link Authentication} has access to the protected
 * service/method. This interface assumes, that the user is authenticated before the method is called.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public interface AccessPredicate extends Predicate<Authentication> {

    @Override
    default AccessPredicate negate() {
        return t -> !test(t);
    }

    @Override
    default AccessPredicate and(final Predicate<? super Authentication> other) {
        requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    @Override
    default AccessPredicate or(final Predicate<? super Authentication> other) {
        requireNonNull(other);
        return t -> test(t) || other.test(t);
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
        return authentication -> true;
    }

    /**
     * All authenticated users can access the protected instance excluding anonymous users.
     *
     * @return A newly created AccessPredicate that checks whether the user is explicitly authenticated.
     */
    static AccessPredicate fullyAuthenticated() {
        return authentication -> !(authentication instanceof AnonymousAuthenticationToken);
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
        return authentication -> false;
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
        return authentication -> {
            for (final GrantedAuthority authority : authentication.getAuthorities()) {
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
        return authentication -> {
            for (final GrantedAuthority authority : authentication.getAuthorities()) {
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
        return authentication -> {
            for (final GrantedAuthority authority : authentication.getAuthorities()) {
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
        return authentication -> {
            for (final GrantedAuthority authority : authentication.getAuthorities()) {
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
        return authentication -> {
            for (final GrantedAuthority authority : authentication.getAuthorities()) {
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
        return authentication -> {
            for (final GrantedAuthority authority : authentication.getAuthorities()) {
                if (!immutableRoles.contains(authority)) {
                    return false;
                }
            }
            return true;
        };
    }

}

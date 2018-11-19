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

package net.devh.boot.grpc.common.security;

import java.nio.charset.StandardCharsets;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;

/**
 * A helper class with constants related to grpc security.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class SecurityConstants {

    /**
     * A convenience constant that contains the key for the HTTP Authorization Header.
     */
    public static final Key<String> AUTHORIZATION_HEADER = Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * The prefix for basic auth as used in the {@link #AUTHORIZATION_HEADER}. This library assumes that the both the
     * username and password are {@link StandardCharsets#UTF_8 UTF_8} encoded before being turned into a base64 string.
     */
    public static final String BASIC_AUTH_PREFIX = "Basic ";

    private SecurityConstants() {}

}
